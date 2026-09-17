package com.pulsegrid.ingestiongateway.application.command;

import java.time.Instant;

public record IngestTelemetryReadingCommand(
        String apiKey,
        String metricType,
        Double value,
        Instant recordedAt
) {
}
