package com.lifeAI.LifeAI.controllers;

import com.lifeAI.LifeAI.services.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/openai")
public class ChatController {

    private final OpenAIService openAIService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestParam("message") String userMessage,
                                  @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Check if a file is uploaded and pass it along with the message
            String response = openAIService.interactWithAssistant(userMessage, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage());
        }
    }

    @PostMapping("/researchSideEffects")
    public ResponseEntity<?> researchSideEffects(@RequestParam("message") String userMessage) {

        String response = openAIService.researchSideEffects(userMessage);
        return ResponseEntity.ok(response);
    }
}