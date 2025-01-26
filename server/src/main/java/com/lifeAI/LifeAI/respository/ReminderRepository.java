package com.lifeAI.LifeAI.respository;

import com.lifeAI.LifeAI.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Reminder findFirstByOrderByIdDesc();
}