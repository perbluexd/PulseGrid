package com.pulsegrid.queryapi.application.model;

import java.time.Instant;

public record TelemetryBucketView(Instant bucketStart, double avg, double min, double max, long sampleCount) {
}
