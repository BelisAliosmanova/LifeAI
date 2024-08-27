package com.lifeAI.LifeAI.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifeAI.LifeAI.exceptions.ErrorProcessingAIResponseException;
import com.lifeAI.LifeAI.exceptions.UnableToExtractContentFromAIResponseException;
import com.lifeAI.LifeAI.services.AIAssistanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIAssistanceServiceImpl implements AIAssistanceService {

    private final RestTemplate restTemplate;
    private final String openAIEndpoint = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public AIAssistanceServiceImpl(RestTemplate restTemplate, @Value("${openai.api.key}") String apiKey, MessageSource messageSource, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    private final String apiUrl = "https://api.openai.com/v1/assistants";

    public String interactWithAssistant(String requestPayload) {
//        String requestBody = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \"" + inputText + "\"}]}";
//
//        // Set headers and create request entity
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + apiKey);
//        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);


        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("OpenAI-Beta", "assistants=v2");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // Add the Authorization header

        // Create the entity with headers and the payload
        HttpEntity<String> entity = new HttpEntity<>(requestPayload, headers);

        // Make the API request
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/assistants/interact",
                entity,
                String.class);

        return response.getBody();
    }

    @Override
    public String generateAnswer(String inputAnswer) {
        try {
            // Create JSON request body using ObjectMapper
            ObjectNode requestBodyNode = objectMapper.createObjectNode();
            requestBodyNode.put("model", "gpt-3.5-turbo");

            // Create the "messages" array and add the user message
            ArrayNode messagesArray = objectMapper.createArrayNode();
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.put("content", inputAnswer);
            messagesArray.add(messageNode);

            // Add the "messages" array to the request body
            requestBodyNode.set("messages", messagesArray);

            // Convert the request body to a JSON string
            String requestBody = objectMapper.writeValueAsString(requestBodyNode);

            // Set headers and create request entity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to OpenAI endpoint
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAIEndpoint, requestEntity, String.class);

            // Process response and return generated text or error message
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                return "Error: " + responseEntity.getStatusCodeValue() + " - " + responseEntity.getBody();
            }
        } catch (Exception e) {
            return "Exception occurred: " + e.getMessage();
        }
    }

    @Override
    public String extractContent(String aiGeneratedContent) {
        try {
            // Parse JSON response to extract generated content
            JsonNode rootNode = objectMapper.readTree(aiGeneratedContent);
            JsonNode messageContent = rootNode.path("choices").get(0).path("message").path("content");
            if (!messageContent.isMissingNode()) {
                return messageContent.asText();
            } else {
                // Throw exception if content cannot be extracted
                throw new UnableToExtractContentFromAIResponseException(messageSource);
            }
        } catch (Exception e) {
            // Handle error while processing AI response
            throw new ErrorProcessingAIResponseException(messageSource);
        }
    }

    @Override
    public String analyzeContent(String prompt) throws JsonProcessingException {
        // Create JSON request body for content analysis
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("role", "user");
        messageNode.put("content", prompt);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", "gpt-4o");
        rootNode.set("messages", objectMapper.createArrayNode().add(messageNode));

        String requestBody = objectMapper.writeValueAsString(rootNode);

        // Set headers and create request entity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Send POST request to OpenAI endpoint for content analysis
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAIEndpoint, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            // Extract content from AI response and return
            if (responseBody != null) {
                return extractContent(responseBody);
            } else {
                throw new UnableToExtractContentFromAIResponseException(messageSource);
            }
        } catch (Exception e) {
            // Handle error while processing AI response
            throw new ErrorProcessingAIResponseException(messageSource);
        }
    }
}
