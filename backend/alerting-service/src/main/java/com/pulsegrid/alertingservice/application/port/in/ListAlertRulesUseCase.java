package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;

import java.util.List;

public interface ListAlertRulesUseCase {
    List<AlertRuleResult> listAll();
}
