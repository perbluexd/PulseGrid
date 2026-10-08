package com.pulsegrid.notificationworkers.infrastructure.notification.email;

import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotification;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotificationSenderPort;
import com.pulsegrid.notificationworkers.infrastructure.notification.email.config.EmailNotificationProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EmailAlertNotificationSenderAdapter implements AlertNotificationSenderPort {

    private final ResilientMailSender resilientMailSender;
    private final EmailNotificationProperties properties;

    @Override
    public void send(AlertNotification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(properties.to());
        message.setSubject("[%s] Alerta en %s: %s".formatted(
                notification.severity(),
                notification.deviceName(),
                notification.metricType()));
        message.setText("""
                Se disparó una alerta en PulseGrid.

                Dispositivo: %s
                Métrica: %s
                Condición: %s %s
                Valor registrado: %s
                Severidad: %s
                Fecha: %s
                Id de alerta: %s
                """.formatted(
                notification.deviceName(),
                notification.metricType(),
                notification.condition(),
                notification.threshold(),
                notification.triggeredValue(),
                notification.severity(),
                notification.triggeredAt(),
                notification.alertId()));
        try {
            resilientMailSender.send(message);
        } catch (MailSendException | CallNotPermittedException | RequestNotPermitted ex) {
            throw new NotificationDeliveryUnavailableException(
                    "Servidor de email no disponible para la alerta " + notification.alertId(), ex);
        }
    }
}
