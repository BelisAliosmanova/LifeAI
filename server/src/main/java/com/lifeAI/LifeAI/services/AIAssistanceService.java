package com.lifeAI.LifeAI.services;

import com.fasterxml.jackson.core.JsonProcessingException;


public interface AIAssistanceService {
    String generateAnswer(String inputAnswer);

    String extractContent(String aiGeneratedContent);

    String analyzeContent(String prompt) throws JsonProcessingException;
    String interactWithAssistant(String input);
}
