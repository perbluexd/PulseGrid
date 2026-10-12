package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LatestReadingsResponse(List<Reading> readings) {

    public record Reading(UUID deviceId, String metricType, double value, Instant recordedAt) {
    }
}
