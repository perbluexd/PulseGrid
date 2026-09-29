package com.pulsegrid.alertingservice.api.dto.alert;

import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID alertId,
        UUID alertRuleId,
        UUID deviceId,
        double triggeredValue,
        AlertSeverity severity,
        AlertStatus status,
        Instant triggeredAt,
        Instant resolvedAt,
        UUID acknowledgedBy,
        UUID resolvedBy
) {
}
