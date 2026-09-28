package com.pulsegrid.alertingservice.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.config.OutboxProperties;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventStatus;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.AlertNotificationRabbitPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private OutboxEventJpaRepository repository;

    @Mock
    private AlertNotificationRabbitPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        OutboxProperties properties = new OutboxProperties(Duration.ofSeconds(2), BATCH_SIZE, "0 0 3 * * *", Duration.ofDays(7));
        relay = new OutboxRelay(repository, publisher, objectMapper, properties);
    }

    @Test
    void shouldPublishPendingEventsAndMarkThemSent() throws Exception {
        OutboxEventEntity first = pendingEvent(notification());
        OutboxEventEntity second = pendingEvent(notification());
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(first, second));

        relay.relayPendingEvents();

        verify(publisher, times(2)).publish(any(AlertTriggeredNotification.class));
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(first.getSentAt()).isNotNull();
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    }

    @Test
    void shouldKeepEventPendingAndStopBatchWhenBrokerFails() throws Exception {
        OutboxEventEntity first = pendingEvent(notification());
        OutboxEventEntity second = pendingEvent(notification());
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(first, second));
        doThrow(new AmqpException("Sin confirmación de RabbitMQ")).when(publisher).publish(any());

        relay.relayPendingEvents();

        verify(publisher, times(1)).publish(any());
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(first.getAttempts()).isEqualTo(1);
        assertThat(first.getLastError()).contains("Sin confirmación");
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(second.getAttempts()).isZero();
    }

    @Test
    void shouldMarkUnreadableEventFailedAndContinueWithTheRest() throws Exception {
        OutboxEventEntity unreadable = OutboxEventEntity.pending(UUID.randomUUID(),
                OutboxAlertNotificationPublisherAdapter.EVENT_TYPE, "{json-roto");
        OutboxEventEntity valid = pendingEvent(notification());
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(unreadable, valid));

        relay.relayPendingEvents();

        verify(publisher, times(1)).publish(any());
        assertThat(unreadable.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(unreadable.getAttempts()).isEqualTo(1);
        assertThat(valid.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    }

    private OutboxEventEntity pendingEvent(AlertTriggeredNotification notification) throws Exception {
        return OutboxEventEntity.pending(notification.alertId(), OutboxAlertNotificationPublisherAdapter.EVENT_TYPE,
                objectMapper.writeValueAsString(notification));
    }

    private AlertTriggeredNotification notification() {
        return new AlertTriggeredNotification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Horno 3",
                "TEMPERATURE", AlertCondition.GREATER_THAN, 80, 92.5, AlertSeverity.CRITICAL, Instant.now());
    }
}
