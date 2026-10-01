package com.pulsegrid.notificationworkers.application.port.in;

import com.pulsegrid.notificationworkers.application.command.ProcessAlertNotificationCommand;

public interface ProcessAlertNotificationUseCase {
    void process(ProcessAlertNotificationCommand command);
}
