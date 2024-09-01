package com.lifeAI.LifeAI.services;


public interface OpenAIService {
    String interactWithAssistant(String userMessage);
    String researchRecommendedStudies(String userMessage);
}
