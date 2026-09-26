package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.UpdateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.error.AlertRuleNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.UpdateAlertRuleUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UpdateAlertRuleService implements UpdateAlertRuleUseCase {
    private final AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Override
    public AlertRuleResult update(UpdateAlertRuleCommand command) {
        AlertRule alertRule = alertRuleRepositoryPort.findById(command.alertRuleId())
                .orElseThrow(() -> new AlertRuleNotFoundException(command.alertRuleId()));

        alertRule.updateDefinition(command.metricType(), command.condition(), command.threshold(), command.severity());
        alertRuleRepositoryPort.save(alertRule);
        return AlertRuleResult.from(alertRule);
    }
}
