package com.pulsegrid.ingestiongateway.application.event;

import java.time.Instant;
import java.util.UUID;

public record TelemetryReadingEvent(
        UUID deviceId,
        String metricType,
        Double value,
        Instant recordedAt
) {
}
