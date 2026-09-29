package com.pulsegrid.alertingservice.application.command;

import java.util.UUID;

public record GetAlertCommand(UUID alertId) {
}
