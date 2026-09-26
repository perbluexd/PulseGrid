package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.CreateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;

public interface CreateAlertRuleUseCase {
    AlertRuleResult create(CreateAlertRuleCommand command);
}
