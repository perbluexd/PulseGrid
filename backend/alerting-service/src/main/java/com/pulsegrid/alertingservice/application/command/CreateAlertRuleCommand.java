package com.pulsegrid.alertingservice.application.command;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;

import java.util.UUID;

public record CreateAlertRuleCommand(
        UUID deviceId,
        UUID groupId,
        String metricType,
        AlertCondition condition,
        double threshold,
        AlertSeverity severity
) {
}
