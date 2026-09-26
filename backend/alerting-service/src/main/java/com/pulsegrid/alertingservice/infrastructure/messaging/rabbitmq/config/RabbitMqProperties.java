package com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rabbitmq.alert-notifications")
public record RabbitMqProperties(
        String exchange,
        String queue,
        String routingKey,
        String deadLetterExchange,
        String deadLetterQueue,
        Duration confirmTimeout
) {
}
