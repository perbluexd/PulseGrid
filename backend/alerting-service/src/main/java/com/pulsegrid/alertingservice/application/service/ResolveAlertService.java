package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.ResolveAlertCommand;
import com.pulsegrid.alertingservice.application.error.AlertNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.ResolveAlertUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;
import com.pulsegrid.alertingservice.domain.model.Alert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ResolveAlertService implements ResolveAlertUseCase {
    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public AlertResult resolve(ResolveAlertCommand command) {
        Alert alert = alertRepositoryPort.findById(command.alertId())
                .orElseThrow(() -> new AlertNotFoundException(command.alertId()));

        alert.resolve(command.userId());
        alertRepositoryPort.save(alert);
        return AlertResult.from(alert);
    }
}
