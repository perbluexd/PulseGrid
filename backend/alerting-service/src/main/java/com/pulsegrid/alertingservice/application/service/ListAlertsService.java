package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.ListAlertsCommand;
import com.pulsegrid.alertingservice.application.port.in.ListAlertsUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ListAlertsService implements ListAlertsUseCase {
    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public List<AlertResult> list(ListAlertsCommand command) {
        return alertRepositoryPort.findAll(command.status(), command.severity(), command.deviceId()).stream()
                .map(AlertResult::from)
                .toList();
    }
}
