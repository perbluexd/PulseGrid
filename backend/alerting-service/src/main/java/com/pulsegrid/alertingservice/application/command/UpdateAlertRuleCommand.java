package com.pulsegrid.alertingservice.application.command;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;

import java.util.UUID;

public record UpdateAlertRuleCommand(
        UUID alertRuleId,
        String metricType,
        AlertCondition condition,
        double threshold,
        AlertSeverity severity
) {
}
