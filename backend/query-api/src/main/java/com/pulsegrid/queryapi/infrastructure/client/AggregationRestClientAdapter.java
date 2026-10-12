package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;
import com.pulsegrid.queryapi.application.port.out.TelemetryPort;
import com.pulsegrid.queryapi.infrastructure.client.dto.LatestReadingsResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.TelemetryHistoryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
public class AggregationRestClientAdapter implements TelemetryPort {

    private static final String SERVICE = "aggregation-service";

    private final RestClient restClient;

    public AggregationRestClientAdapter(@Qualifier("aggregationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<ReadingView> findLatest(Collection<UUID> deviceIds) {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri(uri -> uri.path("/api/v1/internal/telemetry/latest")
                        .queryParam("deviceIds", deviceIds.toArray())
                        .build())
                .retrieve()
                .body(LatestReadingsResponse.class));
        return response.readings().stream()
                .map(r -> new ReadingView(r.deviceId(), r.metricType(), r.value(), r.recordedAt()))
                .toList();
    }

    @Override
    public List<TelemetryBucketView> findHistory(UUID deviceId, String metricType, Instant from, Instant to,
                                                 BucketSize bucketSize) {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri(uri -> uri.path("/api/v1/internal/telemetry/history")
                        .queryParam("deviceId", deviceId)
                        .queryParam("metricType", metricType)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParam("bucketSize", bucketSize)
                        .build())
                .retrieve()
                .body(TelemetryHistoryResponse.class));
        return response.buckets().stream()
                .map(b -> new TelemetryBucketView(b.bucketStart(), b.avgValue(), b.minValue(), b.maxValue(), b.sampleCount()))
                .toList();
    }
}
