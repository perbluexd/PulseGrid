package com.pulsegrid.aggregationservice.application.command;

import com.pulsegrid.aggregationservice.domain.model.BucketSize;

import java.time.Instant;
import java.util.UUID;

public record GetTelemetryHistoryCommand(UUID deviceId, String metricType, Instant from, Instant to, BucketSize bucketSize) {
}
