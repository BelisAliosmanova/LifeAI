package com.lifeAI.LifeAI.controllers;

import com.lifeAI.LifeAI.services.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/openai")
public class ChatController {

    private final OpenAIService openAIService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        String response = openAIService.interactWithAssistant(userMessage);
        return ResponseEntity.ok(response);
    }
}