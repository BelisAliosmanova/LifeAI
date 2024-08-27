package com.lifeAI.LifeAI.services.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    private String apiKey = "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";
    private String assistantId = "asst_FqF4m6Ey4lpsFreuHgSRYLXt";

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String interactWithAssistant(String userMessage) {
        // Step 1: Create a new thread
        String threadId = createNewThread(userMessage);

        // Step 2: Add the user's message to the thread
        addMessageToThread(threadId, userMessage);

        // Step 3: Run the assistant with the thread
        String runId = runAssistant(threadId);

        // Step 4: Optionally, wait for a few seconds before fetching the response
        try {
            Thread.sleep(5000); // Delay for 5 seconds (5000 milliseconds)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Thread was interrupted", e);
        }

        // Step 5: Fetch and return the thread details after the delay
        runAssistantResponse(threadId, runId);
        return getThreadDetails(threadId).toString();
    }


    public String getThreadDetails(String threadId) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId); // Endpoint for fetching a thread

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

//        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().toString();

//            if (responseBody != null) {
//                // Extract messages from the response
//                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("data");
//
//                // Extract content text from each message
//                List<String> contentTexts = messages.stream()
//                        .map(message -> (List<Map<String, Object>>) message.get("content"))
//                        .flatMap(List::stream)
//                        .map(content -> (Map<String, Object>) content.get("text"))
//                        .map(text -> (String) text.get("value"))
//                        .collect(Collectors.toList());
//
//                // Combine content texts into a single string
//                return String.join("\n", contentTexts.getFirst());
//            } else {
//                throw new RuntimeException("Response body is null.");
//            }
//        } else {
//            throw new RuntimeException("Failed to fetch thread details, status code: " + response.getStatusCode());
//        }
    }

    private String createNewThread(String message) {
        String url = "https://api.openai.com/v1/threads"; // Updated endpoint for creating a thread
        HttpHeaders headers = createHeaders();
        headers.set("OpenAI-Beta", "assistants=v2"); // Set the OpenAI-Beta header

        // Define the request body with optional messages
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, String>> messages = new ArrayList<>();

        // Example messages; you can customize or pass empty if not needed
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
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId); // Adjust to the correct endpoint if needed
        HttpHeaders headers = createHeaders();

        // Assuming 'role' should be 'user' for user messages
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
        return responseBody.get("id").toString(); // Assuming the run ID is returned with key "id"
    }

    private void runAssistantResponse(String threadId, String runId) {
        // First, check the status of the run
        String runUrl = String.format("https://api.openai.com/v1/threads/%s/runs/%s", threadId, runId); // Endpoint to get thread run status
        HttpHeaders headers = createHeaders();
        headers.set("OpenAI-Beta", "assistants=v2"); // Ensure the header is set
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        restTemplate.exchange(
                runUrl,
                HttpMethod.GET,
                entity,
                Map.class
        );
    }


    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("OpenAI-Beta", "assistants=v2"); // Add this header
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

