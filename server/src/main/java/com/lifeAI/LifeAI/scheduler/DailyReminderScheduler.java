package com.lifeAI.LifeAI.scheduler;

import com.lifeAI.LifeAI.services.OpenAIService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyReminderScheduler {
    private final OpenAIService openAIService;

    public DailyReminderScheduler(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Every day at 00:00 AM (midnight)
    public void scheduleDailyReminder() {
        System.out.println(openAIService.receiveDailyReminder());
    }
}
