package com.pulsegrid.aggregationservice.application.port.in;

import com.pulsegrid.aggregationservice.application.command.GetLatestReadingsCommand;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;

import java.util.List;

public interface GetLatestReadingsUseCase {
    List<TelemetryReading> getLatest(GetLatestReadingsCommand command);
}
