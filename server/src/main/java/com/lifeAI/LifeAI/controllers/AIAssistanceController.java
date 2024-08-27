package com.lifeAI.LifeAI.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lifeAI.LifeAI.services.AIAssistanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ai")
public class AIAssistanceController {

    private final AIAssistanceService aiAssistanceService;


    @PostMapping("/generate-answer")
    public ResponseEntity<String> generateAnswer(@RequestBody String inputAnswer) {
        String generatedAnswer = aiAssistanceService.generateAnswer(inputAnswer);
        return ResponseEntity.ok(generatedAnswer);
    }

    @PostMapping("/extract-content")
    public ResponseEntity<String> extractContent(@RequestBody String aiGeneratedContent) {
        String extractedContent = aiAssistanceService.extractContent(aiGeneratedContent);
        return ResponseEntity.ok(extractedContent);

    }

    @PostMapping("/analyze-content")
    public ResponseEntity<String> analyzeContent(@RequestBody String prompt) throws JsonProcessingException {
        String analyzedContent = aiAssistanceService.analyzeContent(prompt);
        return ResponseEntity.ok(analyzedContent);
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        return aiAssistanceService.interactWithAssistant(question);
    }
}

