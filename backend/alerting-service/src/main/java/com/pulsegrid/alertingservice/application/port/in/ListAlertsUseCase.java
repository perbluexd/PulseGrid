package com.pulsegrid.alertingservice.application.port.in;

import com.pulsegrid.alertingservice.application.command.ListAlertsCommand;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;

import java.util.List;

public interface ListAlertsUseCase {
    List<AlertResult> list(ListAlertsCommand command);
}
