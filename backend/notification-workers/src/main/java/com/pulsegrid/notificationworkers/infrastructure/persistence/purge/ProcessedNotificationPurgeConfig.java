package com.pulsegrid.notificationworkers.infrastructure.persistence.purge;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ProcessedNotificationPurgeProperties.class)
public class ProcessedNotificationPurgeConfig {
}
