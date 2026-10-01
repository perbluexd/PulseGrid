package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq;

import com.pulsegrid.notificationworkers.application.command.ProcessAlertNotificationCommand;
import com.pulsegrid.notificationworkers.application.port.in.ProcessAlertNotificationUseCase;
import com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.event.AlertTriggeredNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertNotificationListener {

    private final ProcessAlertNotificationUseCase processAlertNotificationUseCase;

    @RabbitListener(queues = "${app.rabbitmq.alert-notifications.queue}")
    public void onAlertTriggered(AlertTriggeredNotification notification) {
        processAlertNotificationUseCase.process(new ProcessAlertNotificationCommand(
                notification.alertId(),
                notification.deviceId(),
                notification.deviceName(),
                notification.metricType(),
                notification.condition(),
                notification.threshold(),
                notification.triggeredValue(),
                notification.severity(),
                notification.triggeredAt()
        ));
    }
}
