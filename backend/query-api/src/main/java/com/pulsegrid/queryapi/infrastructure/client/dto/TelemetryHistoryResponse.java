package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.List;

public record TelemetryHistoryResponse(List<Bucket> buckets) {

    public record Bucket(Instant bucketStart, double avgValue, double minValue, double maxValue, long sampleCount) {
    }
}
