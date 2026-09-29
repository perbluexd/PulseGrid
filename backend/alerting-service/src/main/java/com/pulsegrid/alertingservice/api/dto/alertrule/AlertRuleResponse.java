package com.pulsegrid.alertingservice.api.dto.alertrule;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public record AlertRuleResponse(
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
}
