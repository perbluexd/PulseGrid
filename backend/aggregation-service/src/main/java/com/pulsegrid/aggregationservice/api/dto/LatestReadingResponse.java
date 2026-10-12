package com.pulsegrid.aggregationservice.api.dto;

import java.time.Instant;
import java.util.UUID;

public record LatestReadingResponse(UUID deviceId, String metricType, double value, Instant recordedAt) {
}
