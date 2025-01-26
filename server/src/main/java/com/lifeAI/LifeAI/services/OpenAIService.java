package com.lifeAI.LifeAI.services;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OpenAIService {
    String interactWithAssistant(String userMessage, MultipartFile file) throws IOException;

    String researchSideEffects(String userMessage);
    String receiveDailyReminder();
    String getDailyReminder();
}
