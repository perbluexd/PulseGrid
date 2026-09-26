package com.pulsegrid.alertingservice.domain.model;

import com.pulsegrid.alertingservice.domain.exception.InvalidAlertStatusTransitionException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Alert {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID alertRuleId;
    private final UUID deviceId;
    private final double triggeredValue;
    private final AlertSeverity severity;
    private AlertStatus status;
    private final Instant triggeredAt;
    private Instant resolvedAt;
    private UUID acknowledgedBy;
    private UUID resolvedBy;

    public Alert(UUID id, UUID alertRuleId, UUID deviceId, double triggeredValue, AlertSeverity severity,
                 AlertStatus status, Instant triggeredAt, Instant resolvedAt, UUID acknowledgedBy, UUID resolvedBy) {
        this.id = id;
        this.alertRuleId = alertRuleId;
        this.deviceId = deviceId;
        this.triggeredValue = triggeredValue;
        this.severity = severity;
        this.status = status;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedBy = resolvedBy;
    }

    public static Alert trigger(AlertRule rule, UUID deviceId, double triggeredValue) {
        return new Alert(UUID.randomUUID(), rule.getId(), deviceId, triggeredValue, rule.getSeverity(),
                AlertStatus.TRIGGERED, Instant.now(), null, null, null);
    }

    public void acknowledge(UUID userId) {
        if (status != AlertStatus.TRIGGERED) {
            throw new InvalidAlertStatusTransitionException(status, AlertStatus.ACKNOWLEDGED);
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = userId;
    }

    public void resolve(UUID userId) {
        if (status == AlertStatus.RESOLVED) {
            throw new InvalidAlertStatusTransitionException(status, AlertStatus.RESOLVED);
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedBy = userId;
        this.resolvedAt = Instant.now();
    }

    public boolean isOpen() {
        return status != AlertStatus.RESOLVED;
    }
}
