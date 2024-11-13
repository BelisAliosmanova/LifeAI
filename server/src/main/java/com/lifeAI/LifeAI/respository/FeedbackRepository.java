package com.lifeAI.LifeAI.respository;

import com.lifeAI.LifeAI.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}