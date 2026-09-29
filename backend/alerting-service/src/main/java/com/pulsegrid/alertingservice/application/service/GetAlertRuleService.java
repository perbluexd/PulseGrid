package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.GetAlertRuleCommand;
import com.pulsegrid.alertingservice.application.error.AlertRuleNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.GetAlertRuleUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GetAlertRuleService implements GetAlertRuleUseCase {
    private final AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Override
    public AlertRuleResult get(GetAlertRuleCommand command) {
        return alertRuleRepositoryPort.findById(command.alertRuleId())
                .map(AlertRuleResult::from)
                .orElseThrow(() -> new AlertRuleNotFoundException(command.alertRuleId()));
    }
}
