package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.GetAlertCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;

public interface GetAlertUseCase {
    AlertResult get(GetAlertCommand command);
}
