package com.pulsegrid.notificationworkers.infrastructure.notification.email.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailNotificationProperties.class)
public class EmailNotificationConfig {
}
