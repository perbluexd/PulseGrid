package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.RecordTelemetryReadingCommand;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryReadingRepositoryPort;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecordTelemetryReadingServiceTest {

    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final Instant RECORDED_AT = Instant.parse("2026-09-20T10:15:30Z");

    @Mock
    private TelemetryReadingRepositoryPort telemetryReadingRepositoryPort;

    @InjectMocks
    private RecordTelemetryReadingService recordTelemetryReadingService;

    @Test
    void shouldSaveTelemetryReadingBuiltFromCommand() {
        var command = new RecordTelemetryReadingCommand(DEVICE_ID, "TEMPERATURE", 23.5, RECORDED_AT);

        recordTelemetryReadingService.record(command);

        verify(telemetryReadingRepositoryPort)
                .save(new TelemetryReading(DEVICE_ID, "TEMPERATURE", 23.5, RECORDED_AT));
    }
}
