package com.pulsegrid.notificationworkers.application.service;

import com.pulsegrid.notificationworkers.application.command.ProcessAlertNotificationCommand;
import com.pulsegrid.notificationworkers.application.port.in.ProcessAlertNotificationUseCase;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotification;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotificationSenderPort;
import com.pulsegrid.notificationworkers.application.port.out.ProcessedNotificationRepositoryPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
@Service
public class ProcessAlertNotificationService implements ProcessAlertNotificationUseCase {

    private final ProcessedNotificationRepositoryPort processedNotificationRepositoryPort;
    private final AlertNotificationSenderPort alertNotificationSenderPort;

    @Override
    @Transactional
    public void process(ProcessAlertNotificationCommand command) {
        if (!processedNotificationRepositoryPort.markAsProcessed(command.alertId(), Instant.now())) {
            log.info("Alerta {} ya fue procesada, se ignora el duplicado", command.alertId());
            return;
        }

        alertNotificationSenderPort.send(new AlertNotification(
                command.alertId(),
                command.deviceName(),
                command.metricType(),
                command.condition(),
                command.threshold(),
                command.triggeredValue(),
                command.severity(),
                command.triggeredAt()
        ));
        log.info("Notificación de alerta {} enviada", command.alertId());
    }
}
