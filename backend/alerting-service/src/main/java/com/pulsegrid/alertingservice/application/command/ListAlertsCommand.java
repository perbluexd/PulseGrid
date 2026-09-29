package com.pulsegrid.alertingservice.application.command;

import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;

import java.util.UUID;

public record ListAlertsCommand(AlertStatus status, AlertSeverity severity, UUID deviceId) {
}
