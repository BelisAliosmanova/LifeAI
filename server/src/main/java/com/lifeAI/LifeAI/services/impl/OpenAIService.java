package com.lifeAI.LifeAI.services.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.assistant-id}")
    private String assistantId;

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String interactWithAssistant(String userMessage) throws InterruptedException {
        String threadId = createNewThread(userMessage);
        addMessageToThread(threadId, userMessage);
        String runId = runAssistant(threadId);
        runAssistantResponse(threadId, runId);
        return getThreadDetails(threadId);
    }

    public String getThreadDetails(String threadId) throws InterruptedException {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId); // Endpoint for fetching a thread

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);


        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(response.getBody()).toString(); // Return the response body if the request was successful
        } else {
            throw new RuntimeException("Failed to fetch thread details, status code: " + response.getStatusCode());
        }
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

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }


    private void addMessageToThread(String threadId, String messageContent) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user"); // Add 'role' parameter
        message.put("content", messageContent);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(message, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private String runAssistant(String threadId) {
        String url = String.format("https://api.openai.com/v1/threads/%s/runs", threadId); // Adjust to the correct endpoint if needed
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
                    throw new RuntimeException("Unexpected response structure: " + responseBody);
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

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", "assistants=v2");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

