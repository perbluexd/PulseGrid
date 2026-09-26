package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.port.in.ListAlertRulesUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ListAlertRulesService implements ListAlertRulesUseCase {
    private final AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Override
    public List<AlertRuleResult> listAll() {
        return alertRuleRepositoryPort.findAll().stream().map(AlertRuleResult::from).toList();
    }
}
