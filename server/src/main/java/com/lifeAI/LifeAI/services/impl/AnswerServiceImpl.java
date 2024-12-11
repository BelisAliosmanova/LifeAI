package com.lifeAI.LifeAI.services.impl;

import com.lifeAI.LifeAI.model.Answer;
import com.lifeAI.LifeAI.respository.AnswerRepository;
import com.lifeAI.LifeAI.services.AnswerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class AnswerServiceImpl implements AnswerService {
    private final AnswerRepository answerRepository;

    @Override
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    @Override
    public Answer findById(Long id) {
        return answerRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}