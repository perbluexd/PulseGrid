package com.pulsegrid.alertingservice.infrastructure.messaging.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        int batchSize,
        Duration sentRetention
) {
}
