package com.pulsegrid.alertingservice.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.config.OutboxProperties;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventStatus;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.AlertNotificationRabbitPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventJpaRepository repository;
    private final AlertNotificationRabbitPublisher publisher;
    private final ObjectMapper objectMapper;
    private final OutboxProperties properties;

    @Scheduled(fixedDelayString = "${app.outbox.relay-interval}")
    @Transactional
    public void relayPendingEvents() {
        for (OutboxEventEntity event : repository.lockNextPendingBatch(properties.batchSize())) {
            AlertTriggeredNotification notification;
            try {
                notification = objectMapper.readValue(event.getPayload(), AlertTriggeredNotification.class);
            } catch (JsonProcessingException ex) {
                log.error("Evento outbox {} ilegible, se marca FAILED", event.getId(), ex);
                event.markFailed(ex.getOriginalMessage());
                continue;
            }

            try {
                publisher.publish(notification);
                event.markSent();
            } catch (AmqpException ex) {
                log.warn("No se pudo publicar el evento outbox {} (intento {}): {}",
                        event.getId(), event.getAttempts() + 1, ex.getMessage());
                event.registerFailedAttempt(ex.getMessage());
                return;
            }
        }
    }

    @Scheduled(cron = "${app.outbox.purge-cron}")
    @Transactional
    public void purgeSentEvents() {
        int deleted = repository.deleteByStatusAndSentAtBefore(OutboxEventStatus.SENT,
                Instant.now().minus(properties.sentRetention()));
        if (deleted > 0) {
            log.info("Purgados {} eventos outbox enviados", deleted);
        }
    }
}
