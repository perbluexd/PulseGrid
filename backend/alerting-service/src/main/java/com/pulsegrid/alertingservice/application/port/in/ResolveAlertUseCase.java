package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.ResolveAlertCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;

public interface ResolveAlertUseCase {
    AlertResult resolve(ResolveAlertCommand command);
}
