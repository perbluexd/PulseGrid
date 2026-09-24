package com.pulsegrid.aggregationservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TelemetryReading(UUID deviceId, String metricType, Double value, Instant recordedAt) {
}
