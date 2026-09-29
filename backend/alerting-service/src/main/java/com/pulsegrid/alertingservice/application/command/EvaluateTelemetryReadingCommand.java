package com.pulsegrid.alertingservice.application.command;

import java.time.Instant;
import java.util.UUID;

public record EvaluateTelemetryReadingCommand(UUID deviceId, String metricType, double value, Instant recordedAt) {
}
