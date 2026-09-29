package com.pulsegrid.alertingservice.application.command;

import java.util.UUID;

public record ChangeAlertRuleStatusCommand(UUID alertRuleId, boolean active) {
}
