package com.pulsegrid.queryapi.application.port.in;

import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryQueryUseCase {
    Optional<ReadingView> getLatestReading(UUID deviceId, String metricType);

    Map<UUID, List<ReadingView>> getLatestReadingsByDevice(Collection<UUID> deviceIds);

    List<TelemetryBucketView> getHistory(UUID deviceId, String metricType, Instant from, Instant to, BucketSize bucketSize);
}
