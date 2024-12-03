package com.lifeAI.LifeAI.respository;

import com.lifeAI.LifeAI.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
