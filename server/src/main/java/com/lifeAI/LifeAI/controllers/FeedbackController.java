package com.lifeAI.LifeAI.controllers;

import com.lifeAI.LifeAI.model.Feedback;
import com.lifeAI.LifeAI.services.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(feedbackService.getFeedbackById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback feedbackDTO) {
        Feedback cratedFeedback = feedbackService.createFeedback(feedbackDTO);
        return new ResponseEntity<>(cratedFeedback, HttpStatus.CREATED);
    }
}