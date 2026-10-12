package com.pulsegrid.queryapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.clients")
public record ClientProperties(Duration connectTimeout,
                               Duration readTimeout,
                               String deviceRegistryBaseUrl,
                               String alertingServiceBaseUrl,
                               String aggregationServiceBaseUrl,
                               String notificationWorkersBaseUrl) {
}
