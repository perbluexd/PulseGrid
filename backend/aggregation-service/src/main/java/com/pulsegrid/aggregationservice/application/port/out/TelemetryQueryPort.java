package com.pulsegrid.aggregationservice.application.port.out;

import com.pulsegrid.aggregationservice.domain.model.BucketSize;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TelemetryQueryPort {
    List<TelemetryReading> findLatestByDeviceIds(List<UUID> deviceIds);

    List<MetricBucket> findHistory(UUID deviceId, String metricType, Instant from, Instant to, BucketSize bucketSize);
}
