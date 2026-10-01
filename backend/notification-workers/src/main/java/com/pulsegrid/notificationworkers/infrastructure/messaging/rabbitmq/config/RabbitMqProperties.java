package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq.alert-notifications")
public record RabbitMqProperties(
        String queue,
        String deadLetterExchange,
        String deadLetterQueue
) {
}
