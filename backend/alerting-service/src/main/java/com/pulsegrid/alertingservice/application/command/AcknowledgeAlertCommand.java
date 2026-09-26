package com.pulsegrid.alertingservice.application.command;

import java.util.UUID;

public record AcknowledgeAlertCommand(UUID alertId, UUID userId) {
}
