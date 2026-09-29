package com.pulsegrid.alertingservice.application.port.result;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public record AlertRuleResult(
        UUID alertRuleId,
        UUID deviceId,
        UUID groupId,
        String metricType,
        AlertCondition condition,
        double threshold,
        AlertSeverity severity,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertRuleResult from(AlertRule rule) {
        return new AlertRuleResult(rule.getId(), rule.getDeviceId(), rule.getGroupId(), rule.getMetricType(),
                rule.getCondition(), rule.getThreshold(), rule.getSeverity(), rule.isActive(),
                rule.getCreatedAt(), rule.getUpdatedAt());
    }
}
