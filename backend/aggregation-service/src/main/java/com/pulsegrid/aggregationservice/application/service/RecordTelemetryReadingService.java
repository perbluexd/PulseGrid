package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.RecordTelemetryReadingCommand;
import com.pulsegrid.aggregationservice.application.port.in.RecordTelemetryReadingUseCase;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryReadingRepositoryPort;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RecordTelemetryReadingService implements RecordTelemetryReadingUseCase {
    private final TelemetryReadingRepositoryPort telemetryReadingRepositoryPort;

    @Override
    public void record(RecordTelemetryReadingCommand command) {
        TelemetryReading telemetryReading = new TelemetryReading(
                command.deviceId(),
                command.metricType(),
                command.value(),
                command.recordedAt()
        );
        telemetryReadingRepositoryPort.save(telemetryReading);
    }
}
