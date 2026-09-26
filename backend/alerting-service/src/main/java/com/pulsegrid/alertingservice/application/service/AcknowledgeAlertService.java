package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.AcknowledgeAlertCommand;
import com.pulsegrid.alertingservice.application.error.AlertNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.AcknowledgeAlertUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;
import com.pulsegrid.alertingservice.domain.model.Alert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AcknowledgeAlertService implements AcknowledgeAlertUseCase {
    private final AlertRepositoryPort alertRepositoryPort;

    @Override
    public AlertResult acknowledge(AcknowledgeAlertCommand command) {
        Alert alert = alertRepositoryPort.findById(command.alertId())
                .orElseThrow(() -> new AlertNotFoundException(command.alertId()));

        alert.acknowledge(command.userId());
        alertRepositoryPort.save(alert);
        return AlertResult.from(alert);
    }
}
