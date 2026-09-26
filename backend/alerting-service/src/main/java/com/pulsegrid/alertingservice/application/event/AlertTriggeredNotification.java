package com.pulsegrid.alertingservice.application.event;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public record AlertTriggeredNotification(
        UUID alertId,
        UUID alertRuleId,
        UUID deviceId,
        String deviceName,
        String metricType,
        AlertCondition condition,
        double threshold,
        double triggeredValue,
        AlertSeverity severity,
        Instant triggeredAt
) {
}
