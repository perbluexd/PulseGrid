package com.pulsegrid.notificationworkers.application.command;

import java.time.Instant;
import java.util.UUID;

public record ProcessAlertNotificationCommand(
        UUID alertId,
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
