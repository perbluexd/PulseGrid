package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.GetAlertRuleCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;

public interface GetAlertRuleUseCase {
    AlertRuleResult get(GetAlertRuleCommand command);
}
