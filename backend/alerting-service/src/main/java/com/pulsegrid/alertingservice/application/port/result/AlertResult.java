package com.pulsegrid.alertingservice.application.port.result;

import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResult(
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
    public static AlertResult from(Alert alert) {
        return new AlertResult(alert.getId(), alert.getAlertRuleId(), alert.getDeviceId(), alert.getTriggeredValue(),
                alert.getSeverity(), alert.getStatus(), alert.getTriggeredAt(), alert.getResolvedAt(),
                alert.getAcknowledgedBy(), alert.getResolvedBy());
    }
}
