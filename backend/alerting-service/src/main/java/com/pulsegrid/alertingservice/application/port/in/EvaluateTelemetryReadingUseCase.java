package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.EvaluateTelemetryReadingCommand;

public interface EvaluateTelemetryReadingUseCase {
    void evaluate(EvaluateTelemetryReadingCommand command);
}
