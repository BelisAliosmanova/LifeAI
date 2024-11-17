package com.lifeAI.LifeAI.services.impl;

import com.lifeAI.LifeAI.exceptions.feedback.FeedbackNotFoundException;
import com.lifeAI.LifeAI.model.Feedback;
import com.lifeAI.LifeAI.respository.FeedbackRepository;
import com.lifeAI.LifeAI.services.FeedbackService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Override
    public List<Feedback> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream().map(feedback -> modelMapper.map(feedback, Feedback.class)).toList();
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            return modelMapper.map(feedback.get(), Feedback.class);
        }
        throw new FeedbackNotFoundException(messageSource);
    }

    @Override
    public Feedback createFeedback(Feedback feedbackDTO) {
        feedbackDTO.setId(null);
        feedbackDTO.setCreatedAt(LocalDateTime.now());
        feedbackDTO.setComment(feedbackDTO.getComment());
        feedbackDTO.setThumbsUp(feedbackDTO.isThumbsUp());
        feedbackDTO.setUrl(feedbackDTO.getUrl());

        Feedback feedbackEntity = feedbackRepository.save(modelMapper.map(feedbackDTO, Feedback.class));
        return modelMapper.map(feedbackEntity, Feedback.class);
    }
}