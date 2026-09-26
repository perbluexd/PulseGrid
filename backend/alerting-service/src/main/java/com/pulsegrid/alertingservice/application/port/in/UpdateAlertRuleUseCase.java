package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.UpdateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;

public interface UpdateAlertRuleUseCase {
    AlertRuleResult update(UpdateAlertRuleCommand command);
}
