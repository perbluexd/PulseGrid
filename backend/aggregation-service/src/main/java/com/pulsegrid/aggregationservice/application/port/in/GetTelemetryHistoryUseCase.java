package com.pulsegrid.aggregationservice.application.port.in;

import com.pulsegrid.aggregationservice.application.command.GetTelemetryHistoryCommand;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;

import java.util.List;

public interface GetTelemetryHistoryUseCase {
    List<MetricBucket> getHistory(GetTelemetryHistoryCommand command);
}
