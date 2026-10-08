package com.pulsegrid.notificationworkers.infrastructure.notification.email;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ResilientMailSender {

    public static final String EMAIL = "email";

    private final JavaMailSender mailSender;

    @Retry(name = EMAIL)
    @CircuitBreaker(name = EMAIL)
    @RateLimiter(name = EMAIL)
    public void send(SimpleMailMessage message) {
        mailSender.send(message);
    }
}
