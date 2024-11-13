package com.lifeAI.LifeAI.exceptions.feedback;

import com.lifeAI.LifeAI.exceptions.common.NoSuchElementException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class FeedbackNotFoundException extends NoSuchElementException {
    public FeedbackNotFoundException(MessageSource messageSource) {
        super(messageSource.getMessage("feedback.not.found", null, LocaleContextHolder.getLocale()));
    }
}
