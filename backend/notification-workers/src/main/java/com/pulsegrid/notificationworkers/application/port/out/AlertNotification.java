package com.pulsegrid.notificationworkers.application.port.out;

import java.time.Instant;
import java.util.UUID;

public record AlertNotification(
        UUID alertId,
        String deviceName,
        String metricType,
        String condition,
        double threshold,
        double triggeredValue,
        String severity,
        Instant triggeredAt
) {
}
