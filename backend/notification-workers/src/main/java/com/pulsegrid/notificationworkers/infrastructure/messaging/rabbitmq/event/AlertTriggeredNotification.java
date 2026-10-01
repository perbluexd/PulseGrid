package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.event;

import java.time.Instant;
import java.util.UUID;

public record AlertTriggeredNotification(
        UUID alertId,
        UUID alertRuleId,
        UUID deviceId,
        String deviceName,
        String metricType,
        String condition,
        double threshold,
        double triggeredValue,
        String severity,
        Instant triggeredAt
) {
}
