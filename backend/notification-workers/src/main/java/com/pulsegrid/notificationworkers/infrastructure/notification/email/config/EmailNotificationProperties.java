package com.pulsegrid.notificationworkers.infrastructure.notification.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifications.email")
public record EmailNotificationProperties(
        String from,
        String to
) {
}
