package com.pulsegrid.alertingservice.application.port.out;

import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;

public interface AlertNotificationPublisherPort {
    void publish(AlertTriggeredNotification notification);
}
