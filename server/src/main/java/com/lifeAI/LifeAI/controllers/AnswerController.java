package com.lifeAI.LifeAI.controllers;

import com.lifeAI.LifeAI.model.Answer;
import com.lifeAI.LifeAI.services.AnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/answers")
public class AnswerController {
    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Answer>> getAllAnswers() {
        return ResponseEntity.ok(answerService.getAllAnswers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswerById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(answerService.findById(id));
    }
}
