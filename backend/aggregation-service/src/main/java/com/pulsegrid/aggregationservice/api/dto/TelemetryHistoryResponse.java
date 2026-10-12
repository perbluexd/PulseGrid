package com.pulsegrid.aggregationservice.api.dto;

import java.util.List;
import java.util.UUID;

public record TelemetryHistoryResponse(UUID deviceId, String metricType, String bucketSize, List<MetricBucketResponse> buckets) {
}
