package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.GetLatestReadingsCommand;
import com.pulsegrid.aggregationservice.application.port.in.GetLatestReadingsUseCase;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryQueryPort;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class GetLatestReadingsService implements GetLatestReadingsUseCase {

    private final TelemetryQueryPort telemetryQueryPort;

    @Override
    public List<TelemetryReading> getLatest(GetLatestReadingsCommand command) {
        if (command.deviceIds().isEmpty()) {
            return List.of();
        }
        return telemetryQueryPort.findLatestByDeviceIds(command.deviceIds().stream().distinct().toList());
    }
}
