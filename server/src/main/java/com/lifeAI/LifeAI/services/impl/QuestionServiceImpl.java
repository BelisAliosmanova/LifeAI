package com.lifeAI.LifeAI.services.impl;

import com.lifeAI.LifeAI.model.Question;
import com.lifeAI.LifeAI.respository.QuestionRepository;
import com.lifeAI.LifeAI.services.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository repository;

    @Override
    public List<Question> getAllQuestions() {
        return repository.findAll();
    }

    @Override
    public List<Question> getRootQuestions() {
        return repository.findAll()
                .stream()
                .filter(q -> q.getDependsOn() == null)
                .collect(Collectors.toList());
    }
}
