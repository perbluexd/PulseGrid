package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.ChangeAlertRuleStatusCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;

public interface ChangeAlertRuleStatusUseCase {
    AlertRuleResult changeStatus(ChangeAlertRuleStatusCommand command);
}
