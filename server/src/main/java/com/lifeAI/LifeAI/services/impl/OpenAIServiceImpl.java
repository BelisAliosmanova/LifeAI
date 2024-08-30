package com.lifeAI.LifeAI.services.impl;
import com.lifeAI.LifeAI.exceptions.ErrorProcessingAIResponseException;
import com.lifeAI.LifeAI.services.OpenAIService;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.assistant-id}")
    private String assistantId;

    private final RestTemplate restTemplate;

    private final MessageSource messageSource;
    private  final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    public OpenAIServiceImpl(RestTemplate restTemplate, MessageSource messageSource, VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.restTemplate = restTemplate;
        this.messageSource = messageSource;
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    @Override
    public String interactWithAssistant(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            throw new ErrorProcessingAIResponseException(messageSource);
        }

        Document userMessageDoc = new Document(userMessage);
        vectorStore.add(tokenTextSplitter.apply(List.of(userMessageDoc)));

        // Query vector database for relevant pages
        List<Document> relevantEntries = vectorStore.similaritySearch(String.valueOf(userMessageDoc));

        // Combine relevant content with user message
        StringBuilder contextBuilder = new StringBuilder();
        for (Document entry : relevantEntries) {
            contextBuilder.append(entry.getContent());
        }

        // Use the combined message to interact with the assistant
        String combinedMessage = contextBuilder + "User: " + userMessage;
        String threadId = createNewThread(combinedMessage);
        addMessageToThread(threadId, combinedMessage);
        String runId = runAssistant(threadId);
        runAssistantResponse(threadId, runId);
        return getFullAssistantResponseText(threadId);
    }

    private String createNewThread(String message) {
        String url = "https://api.openai.com/v1/threads";
        HttpHeaders headers = createHeaders();
        headers.set("OpenAI-Beta", "assistants=v2");

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(createMessage("user", message));

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("id")) {
            return responseBody.get("id").toString(); // Return the thread ID
        } else {
            throw new RuntimeException("Failed to create a new thread, no ID returned.");
        }
    }

    private void addMessageToThread(String threadId, String messageContent) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", messageContent);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(message, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private String runAssistant(String threadId) {
        String url = String.format("https://api.openai.com/v1/threads/%s/runs", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, String> body = Collections.singletonMap("assistant_id", assistantId);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        return responseBody.get("id").toString();
    }

    public void runAssistantResponse(String threadId, String runId) {
        String runUrl = String.format("https://api.openai.com/v1/threads/%s/runs/%s", threadId, runId);
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        boolean isInProgress = true;

        while (isInProgress) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        runUrl,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                // Extracting status from response
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("status")) {
                    String status = (String) responseBody.get("status");

                    // Check if status is still in progress
                    if ("in_progress".equals(status)) {
                        System.out.println("Status: in progress. Waiting...");
                        Thread.sleep(5000); // Wait for 5 seconds before checking again
                    } else {
                        isInProgress = false;
                        System.out.println("Status: " + status);
                    }
                } else {
                    throw new ErrorProcessingAIResponseException(messageSource);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("Error while checking run status: " + e.getMessage());
                break;
            }
        }
    }

    private String getFullAssistantResponseText(String threadId) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId);

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBodyString = Objects.requireNonNull(response).toString();

            int valueStartIndex = responseBodyString.indexOf("value=");
            if (valueStartIndex == -1) {
                throw new IllegalStateException("Could not find 'value=' in the response.");
            }

            valueStartIndex += "value=".length();

            int annotationsStartIndex = responseBodyString.indexOf("annotations", valueStartIndex);
            if (annotationsStartIndex == -1) {
                annotationsStartIndex = responseBodyString.length();
            }

            return responseBodyString.substring(valueStartIndex, annotationsStartIndex).trim();
        } else {
            throw new ErrorProcessingAIResponseException(messageSource);
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", "assistants=v2");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
