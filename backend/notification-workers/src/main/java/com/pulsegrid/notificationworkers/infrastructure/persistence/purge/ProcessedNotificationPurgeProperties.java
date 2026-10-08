package com.pulsegrid.notificationworkers.infrastructure.persistence.purge;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.processed-notifications")
public record ProcessedNotificationPurgeProperties(
        String purgeCron,
        Duration retention
) {
}
