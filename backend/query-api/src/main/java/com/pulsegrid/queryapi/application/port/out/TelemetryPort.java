package com.pulsegrid.queryapi.application.port.out;

import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TelemetryPort {
    List<ReadingView> findLatest(Collection<UUID> deviceIds);

    List<TelemetryBucketView> findHistory(UUID deviceId, String metricType, Instant from, Instant to, BucketSize bucketSize);
}
