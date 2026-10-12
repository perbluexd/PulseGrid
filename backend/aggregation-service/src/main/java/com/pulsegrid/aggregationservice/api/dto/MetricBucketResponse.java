package com.pulsegrid.aggregationservice.api.dto;

import java.time.Instant;

public record MetricBucketResponse(Instant bucketStart, double avgValue, double minValue, double maxValue, long sampleCount) {
}
