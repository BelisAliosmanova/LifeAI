package com.lifeAI.LifeAI.services;

import com.lifeAI.LifeAI.model.Answer;

import java.util.List;

public interface AnswerService {
    List<Answer> getAllAnswers();
    Answer findById(Long id);
}
