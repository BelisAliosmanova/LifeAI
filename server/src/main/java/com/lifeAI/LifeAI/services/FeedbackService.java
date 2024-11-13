package com.lifeAI.LifeAI.services;

import com.lifeAI.LifeAI.model.Feedback;

import java.util.List;

public interface FeedbackService {
    List<Feedback> getAllFeedbacks();
    Feedback getFeedbackById(Long id);
    Feedback createFeedback(Feedback feedbackDTO);
}
