package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceStatusChangedEvent;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.infrastructure.messaging.DeviceRegistryEventKafkaPublisher;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.config.OutboxProperties;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity.OutboxEventStatus;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private OutboxEventJpaRepository repository;

    @Mock
    private DeviceRegistryEventKafkaPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        OutboxProperties properties = new OutboxProperties(BATCH_SIZE, Duration.ofDays(7));
        relay = new OutboxRelay(repository, publisher, objectMapper, properties);
    }

    @Test
    void shouldSendPendingEventsWithDeviceIdAsKeyAndMarkThemSent() throws Exception {
        ApiKeyRotatedEvent rotated = apiKeyRotatedEvent();
        DeviceStatusChangedEvent statusChanged = deviceStatusChangedEvent();
        OutboxEventEntity first = pendingEvent(rotated.deviceId(), OutboxEventType.API_KEY_ROTATED, rotated);
        OutboxEventEntity second = pendingEvent(statusChanged.deviceId(), OutboxEventType.DEVICE_STATUS_CHANGED, statusChanged);
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(first, second));
        when(publisher.send(anyString(), any())).thenReturn(succeeded());

        relay.relayPendingEvents();

        verify(publisher).send(eq(rotated.deviceId().toString()), eq(rotated));
        verify(publisher).send(eq(statusChanged.deviceId().toString()), isA(DeviceStatusChangedEvent.class));
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        assertThat(first.getSentAt()).isNotNull();
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    }

    @Test
    void shouldKeepFailedEventAndTheRestOfTheBatchPendingWhenKafkaFails() throws Exception {
        ApiKeyRotatedEvent event = apiKeyRotatedEvent();
        OutboxEventEntity first = pendingEvent(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
        OutboxEventEntity second = pendingEvent(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(first, second));
        when(publisher.send(anyString(), any()))
                .thenReturn(failsLater(new TimeoutException("Expiring 2 record(s)")))
                .thenReturn(succeeded());

        relay.relayPendingEvents();

        verify(publisher, times(2)).send(anyString(), any());
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(first.getAttempts()).isEqualTo(1);
        assertThat(first.getLastError()).contains("Expiring");
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(second.getAttempts()).isZero();
    }

    @Test
    void shouldStopSendingTheBatchWhenSendFailsImmediately() throws Exception {
        ApiKeyRotatedEvent event = apiKeyRotatedEvent();
        OutboxEventEntity first = pendingEvent(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
        OutboxEventEntity second = pendingEvent(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(first, second));
        when(publisher.send(anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new TimeoutException("Topic not present in metadata")));

        relay.relayPendingEvents();

        verify(publisher, times(1)).send(anyString(), any());
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(first.getAttempts()).isEqualTo(1);
        assertThat(first.getLastError()).contains("metadata");
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(second.getAttempts()).isZero();
    }

    @Test
    void shouldMarkUnreadableEventFailedAndContinueWithTheRest() throws Exception {
        ApiKeyRotatedEvent event = apiKeyRotatedEvent();
        OutboxEventEntity unreadable = OutboxEventEntity.pending(UUID.randomUUID(),
                OutboxEventType.API_KEY_ROTATED.name(), "{json-roto");
        OutboxEventEntity unknownType = OutboxEventEntity.pending(UUID.randomUUID(), "DEVICE_EXPLODED", "{}");
        OutboxEventEntity valid = pendingEvent(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
        when(repository.lockNextPendingBatch(BATCH_SIZE)).thenReturn(List.of(unreadable, unknownType, valid));
        when(publisher.send(anyString(), any())).thenReturn(succeeded());

        relay.relayPendingEvents();

        verify(publisher, times(1)).send(anyString(), any());
        assertThat(unreadable.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(unknownType.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(valid.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    }

    private OutboxEventEntity pendingEvent(UUID deviceId, OutboxEventType type, Object event) throws Exception {
        return OutboxEventEntity.pending(deviceId, type.name(), objectMapper.writeValueAsString(event));
    }

    private CompletableFuture<SendResult<String, Object>> succeeded() {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<SendResult<String, Object>> failsLater(Throwable error) {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS)
                .execute(() -> future.completeExceptionally(error));
        return future;
    }

    private ApiKeyRotatedEvent apiKeyRotatedEvent() {
        return new ApiKeyRotatedEvent(UUID.randomUUID(), UUID.randomUUID(), "revoked-hash", UUID.randomUUID(), Instant.now());
    }

    private DeviceStatusChangedEvent deviceStatusChangedEvent() {
        return new DeviceStatusChangedEvent(UUID.randomUUID(), DeviceStatus.INACTIVE, "active-hash", Instant.now());
    }
}
