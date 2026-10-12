package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.GetTelemetryHistoryCommand;
import com.pulsegrid.aggregationservice.application.error.InvalidTimeRangeException;
import com.pulsegrid.aggregationservice.application.port.in.GetTelemetryHistoryUseCase;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryQueryPort;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@AllArgsConstructor
@Service
public class GetTelemetryHistoryService implements GetTelemetryHistoryUseCase {

    private static final Duration MAX_RANGE = Duration.ofDays(31);

    private final TelemetryQueryPort telemetryQueryPort;

    @Override
    public List<MetricBucket> getHistory(GetTelemetryHistoryCommand command) {
        if (!command.from().isBefore(command.to())) {
            throw new InvalidTimeRangeException("'from' debe ser anterior a 'to'");
        }
        if (Duration.between(command.from(), command.to()).compareTo(MAX_RANGE) > 0) {
            throw new InvalidTimeRangeException("El rango máximo es de 31 días");
        }
        return telemetryQueryPort.findHistory(command.deviceId(), command.metricType(),
                command.from(), command.to(), command.bucketSize());
    }
}
