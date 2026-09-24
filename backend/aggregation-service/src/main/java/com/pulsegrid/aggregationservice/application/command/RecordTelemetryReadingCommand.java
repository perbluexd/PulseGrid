package com.pulsegrid.aggregationservice.application.command;

import java.time.Instant;
import java.util.UUID;

public record RecordTelemetryReadingCommand(UUID deviceId, String metricType, Double value, Instant recordedAt) {
}
