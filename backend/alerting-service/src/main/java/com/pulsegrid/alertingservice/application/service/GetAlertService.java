package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.GetAlertCommand;
import com.pulsegrid.alertingservice.application.error.AlertNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.GetAlertUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GetAlertService implements GetAlertUseCase {
    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public AlertResult get(GetAlertCommand command) {
        return alertRepositoryPort.findById(command.alertId())
                .map(AlertResult::from)
                .orElseThrow(() -> new AlertNotFoundException(command.alertId()));
    }
}
