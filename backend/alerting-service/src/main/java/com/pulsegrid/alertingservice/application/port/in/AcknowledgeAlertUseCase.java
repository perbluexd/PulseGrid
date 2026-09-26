package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.AcknowledgeAlertCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;

public interface AcknowledgeAlertUseCase {
    AlertResult acknowledge(AcknowledgeAlertCommand command);
}
