package com.lifeAI.LifeAI.services;

import com.lifeAI.LifeAI.model.Question;

import java.util.List;

public interface QuestionService {
    List<Question> getAllQuestions();
    List<Question> getRootQuestions();
}
