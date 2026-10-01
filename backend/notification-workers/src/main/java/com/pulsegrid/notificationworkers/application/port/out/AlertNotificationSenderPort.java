package com.pulsegrid.notificationworkers.application.port.out;

public interface AlertNotificationSenderPort {
    void send(AlertNotification notification);
}
