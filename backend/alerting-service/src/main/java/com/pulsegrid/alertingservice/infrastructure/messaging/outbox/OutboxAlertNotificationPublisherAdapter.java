package com.pulsegrid.alertingservice.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.application.port.out.AlertNotificationPublisherPort;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxAlertNotificationPublisherAdapter implements AlertNotificationPublisherPort {

    public static final String EVENT_TYPE = "AlertTriggered";

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(AlertTriggeredNotification notification) {
        repository.save(OutboxEventEntity.pending(notification.alertId(), EVENT_TYPE, toJson(notification)));
    }

    private String toJson(AlertTriggeredNotification notification) {
        try {
            return objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar la notificación " + notification.alertId(), ex);
        }
    }
}
