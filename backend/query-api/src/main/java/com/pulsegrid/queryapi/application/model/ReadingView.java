package com.pulsegrid.queryapi.application.model;

import java.time.Instant;
import java.util.UUID;

public record ReadingView(UUID deviceId, String metricType, double value, Instant recordedAt) {
}
