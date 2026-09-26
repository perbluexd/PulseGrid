package com.pulsegrid.alertingservice.domain.model;

import com.pulsegrid.alertingservice.domain.exception.InvalidAlertRuleTargetException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertRule {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID deviceId;
    private final UUID groupId;
    private String metricType;
    private AlertCondition condition;
    private double threshold;
    private AlertSeverity severity;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public AlertRule(UUID id, UUID deviceId, UUID groupId, String metricType, AlertCondition condition,
                     double threshold, AlertSeverity severity, boolean active, Instant createdAt, Instant updatedAt) {
        if ((deviceId == null) == (groupId == null)) {
            throw new InvalidAlertRuleTargetException();
        }
        this.id = id;
        this.deviceId = deviceId;
        this.groupId = groupId;
        this.metricType = metricType;
        this.condition = condition;
        this.threshold = threshold;
        this.severity = severity;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AlertRule create(UUID deviceId, UUID groupId, String metricType, AlertCondition condition,
                                   double threshold, AlertSeverity severity) {
        Instant now = Instant.now();
        return new AlertRule(UUID.randomUUID(), deviceId, groupId, metricType, condition, threshold, severity,
                true, now, now);
    }

    public void updateDefinition(String metricType, AlertCondition condition, double threshold, AlertSeverity severity) {
        this.metricType = metricType;
        this.condition = condition;
        this.threshold = threshold;
        this.severity = severity;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public boolean isTriggeredBy(String readingMetricType, double value) {
        return active && metricType.equals(readingMetricType) && condition.isMetBy(value, threshold);
    }
}
