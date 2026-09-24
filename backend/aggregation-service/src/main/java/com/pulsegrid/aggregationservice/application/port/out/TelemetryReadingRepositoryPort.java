package com.pulsegrid.aggregationservice.application.port.out;

import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;

public interface TelemetryReadingRepositoryPort {
    void save(TelemetryReading telemetryReading);
}
