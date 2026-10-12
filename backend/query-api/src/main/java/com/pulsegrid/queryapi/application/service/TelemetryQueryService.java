package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;
import com.pulsegrid.queryapi.application.port.in.TelemetryQueryUseCase;
import com.pulsegrid.queryapi.application.port.out.TelemetryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TelemetryQueryService implements TelemetryQueryUseCase {

    private final TelemetryPort telemetryPort;

    @Override
    public Optional<ReadingView> getLatestReading(UUID deviceId, String metricType) {
        if (deviceId == null) {
            throw new InvalidQueryArgumentException("'deviceId' no es un UUID válido");
        }
        return telemetryPort.findLatest(List.of(deviceId)).stream()
                .filter(reading -> reading.metricType().equals(metricType))
                .findFirst();
    }

    @Override
    public Map<UUID, List<ReadingView>> getLatestReadingsByDevice(Collection<UUID> deviceIds) {
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        return telemetryPort.findLatest(deviceIds).stream()
                .collect(Collectors.groupingBy(ReadingView::deviceId));
    }

    @Override
    public List<TelemetryBucketView> getHistory(UUID deviceId, String metricType, Instant from, Instant to,
                                                BucketSize bucketSize) {
        if (deviceId == null) {
            throw new InvalidQueryArgumentException("'deviceId' no es un UUID válido");
        }
        if (!from.isBefore(to)) {
            throw new InvalidQueryArgumentException("'from' debe ser anterior a 'to'");
        }
        return telemetryPort.findHistory(deviceId, metricType, from, to, bucketSize);
    }
}
