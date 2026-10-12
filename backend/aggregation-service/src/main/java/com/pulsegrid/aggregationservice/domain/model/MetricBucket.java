package com.pulsegrid.aggregationservice.domain.model;

import java.time.Instant;

public record MetricBucket(Instant bucketStart, double avgValue, double minValue, double maxValue, long sampleCount) {
}
