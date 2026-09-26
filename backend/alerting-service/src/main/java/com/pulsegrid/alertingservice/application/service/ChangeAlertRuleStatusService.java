package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.ChangeAlertRuleStatusCommand;
import com.pulsegrid.alertingservice.application.error.AlertRuleNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.ChangeAlertRuleStatusUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChangeAlertRuleStatusService implements ChangeAlertRuleStatusUseCase {
    private final AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Override
    public AlertRuleResult changeStatus(ChangeAlertRuleStatusCommand command) {
        AlertRule alertRule = alertRuleRepositoryPort.findById(command.alertRuleId())
                .orElseThrow(() -> new AlertRuleNotFoundException(command.alertRuleId()));

        if (command.active()) {
            alertRule.activate();
        } else {
            alertRule.deactivate();
        }
        alertRuleRepositoryPort.save(alertRule);
        return AlertRuleResult.from(alertRule);
    }
}
