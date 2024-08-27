package com.lifeAI.LifeAI.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

/**
 * Interceptor class responsible for setting the locale based on the incoming request's locale.
 */
public class LocaleInterceptor implements HandlerInterceptor {


    public boolean preHandle(HttpServletRequest request) throws Exception {
        Locale locale = request.getLocale();
        LocaleContextHolder.setLocale(locale);
        return true;
    }
}

