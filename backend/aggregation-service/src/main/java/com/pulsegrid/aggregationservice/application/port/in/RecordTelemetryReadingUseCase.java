package com.pulsegrid.aggregationservice.application.port.in;

import com.pulsegrid.aggregationservice.application.command.RecordTelemetryReadingCommand;

public interface RecordTelemetryReadingUseCase {
    void record(RecordTelemetryReadingCommand command);
}
