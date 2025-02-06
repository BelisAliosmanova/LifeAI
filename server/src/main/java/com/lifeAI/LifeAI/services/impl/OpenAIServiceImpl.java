package com.lifeAI.LifeAI.services.impl;

import com.lifeAI.LifeAI.exceptions.ai.ErrorProcessingAIResponseException;
import com.lifeAI.LifeAI.model.Reminder;
import com.lifeAI.LifeAI.respository.ReminderRepository;
import com.lifeAI.LifeAI.services.OpenAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final RestTemplate restTemplate;
    private final MessageSource messageSource;
    private final ReminderRepository reminderRepository;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    @Value("${spring.ai.openai.assistant-id}")
    private String assistantId;
    @Value("${spring.ai.openai.positiveAssistant-id}")
    private String positiveAssistantId;

    public OpenAIServiceImpl(RestTemplate restTemplate, MessageSource messageSource, ReminderRepository reminderRepository) {
        this.restTemplate = restTemplate;
        this.messageSource = messageSource;
        this.reminderRepository = reminderRepository;
    }

    @Override
    public String interactWithAssistant(String userMessage, MultipartFile file) throws IOException {
        if (userMessage == null || userMessage.isEmpty()) {
            throw new ErrorProcessingAIResponseException(messageSource);
        }

        String threadId = "";
        String fileId = null;

        if (file != null && !file.isEmpty()) {
            fileId = uploadFileToOpenAI(file);
            threadId = createNewThread("Processing file with ID: " + fileId);

            List<Map<String, Object>> contentList = new ArrayList<>();
            contentList.add(createFileMessage(fileId));
            contentList.add(createTextMessage(userMessage));

            addMessageContentToThread(threadId, contentList);
        } else {
            // If no file, just create a thread with the user message
            threadId = createNewThread(userMessage);
            addMessageToThread(threadId, userMessage);
        }

        String runId = runAssistant(threadId);
        runAssistantResponse(threadId, runId);

        return getFullAssistantResponseText(threadId);
    }

    @Override
    public String researchSideEffects(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            throw new ErrorProcessingAIResponseException(messageSource);
        }

        String threadId = createNewThread("The patient has these side effects from the breast cancer, help finding solutions: "
                + userMessage);
        addMessageToThread(threadId, userMessage);
        String runId = runAssistant(threadId);
        runAssistantResponse(threadId, runId);
        return getFullAssistantResponseText(threadId);
    }

    @Override
    public String receiveDailyReminder() {
        String threadId = createNewThread("Please give a positive reminder: ");
        String runId = runPositiveAssistant(threadId);
        runAssistantResponse(threadId, runId);

        String response = getFullAssistantResponseText(threadId);
        reminderRepository.save(new Reminder(response));

        return response;
    }

    @Override
    public String getDailyReminder() {
        return reminderRepository.findFirstByOrderByIdDesc().getText();
    }

    private Map<String, Object> createFileMessage(String fileId) {
        Map<String, Object> fileMessage = new HashMap<>();
        fileMessage.put("type", "image_file");

        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("file_id", fileId);

        fileMessage.put("image_file", imageFile);
        return fileMessage;
    }

    private Map<String, Object> createTextMessage(String text) {
        Map<String, Object> textMessage = new HashMap<>();
        textMessage.put("type", "text");
        textMessage.put("text", text);
        return textMessage;
    }

    private void addMessageContentToThread(String threadId, List<Map<String, Object>> contentList) {
        String url = String.format("https://turbo.gptboost.io/v1/threads/%s/messages", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");
        messageContent.put("content", contentList);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(messageContent, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }


    private String uploadFileToOpenAI(MultipartFile file) throws IOException {
        String url = "https://turbo.gptboost.io/v1/files";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        body.add("purpose", "assistants");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("id")) {
            return responseBody.get("id").toString();
        } else {
            throw new RuntimeException("Failed to upload file, no ID returned.");
        }
    }

    private String createNewThread(String message) {
        String url = "https://turbo.gptboost.io/v1/threads";
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
        String url = String.format("https://turbo.gptboost.io/v1/threads/%s/messages", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", messageContent);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(message, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private String runAssistant(String threadId) {
        String url = String.format("https://turbo.gptboost.io/v1/threads/%s/runs", threadId);
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

    private String runPositiveAssistant(String threadId) {
        String url = String.format("https://turbo.gptboost.io/v1/threads/%s/runs", threadId);
        HttpHeaders headers = createHeaders();

        Map<String, String> body = Collections.singletonMap("assistant_id", positiveAssistantId);
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
        String runUrl = String.format("https://turbo.gptboost.io/v1/threads/%s/runs/%s", threadId, runId);
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        boolean isInProgress = true;
        int maxRetries = 10; // Maximum number of retries for queued status
        int retryCount = 0;

        while (isInProgress && retryCount < maxRetries) {
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

                    if ("in_progress".equals(status)) {
                        System.out.println("Status: in progress. Waiting...");
                        Thread.sleep(5000); // Wait for 5 seconds before checking again
                    } else if ("queued".equals(status)) {
                        retryCount++;
                        System.out.println("Status: queued. Retry attempt: " + retryCount);
                        Thread.sleep(5000); // Wait before retrying
                    } else {
                        isInProgress = false;
                        System.out.println("Final Status: " + status);
                    }
                } else {
                    System.err.println("Unexpected response: " + responseBody);
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

        if (retryCount >= maxRetries) {
            System.err.println("Status stuck on queued. Exceeded retry limit.");
        }
    }

    private String getFullAssistantResponseText(String threadId) {
        String url = String.format("https://turbo.gptboost.io/v1/threads/%s/messages", threadId);

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

    // Helper class to wrap InputStream as a Resource for RestTemplate
    private static class MultipartInputStreamFileResource extends InputStreamResource {
        private final String filename;

        MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1;  // Unable to determine, let the framework decide
        }
    }
}

