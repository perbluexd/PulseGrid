package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.infrastructure.messaging.DeviceRegistryEventKafkaPublisher;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.config.OutboxProperties;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity.OutboxEventStatus;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventJpaRepository repository;
    private final DeviceRegistryEventKafkaPublisher publisher;
    private final ObjectMapper objectMapper;
    private final OutboxProperties properties;

    @Scheduled(fixedDelayString = "${app.outbox.relay-interval}")
    @Transactional
    public void relayPendingEvents() {
        Map<OutboxEventEntity, CompletableFuture<SendResult<String, Object>>> inFlight = new LinkedHashMap<>();

        for (OutboxEventEntity event : repository.lockNextPendingBatch(properties.batchSize())) {
            Object payload;
            try {
                payload = objectMapper.readValue(event.getPayload(),
                        OutboxEventType.valueOf(event.getEventType()).eventClass());
            } catch (JsonProcessingException | IllegalArgumentException ex) {
                log.error("Evento outbox {} ilegible, se marca FAILED", event.getId(), ex);
                event.markFailed(ex.getMessage());
                continue;
            }
            inFlight.put(event, publisher.send(event.getAggregateId().toString(), payload));
        }

        for (Map.Entry<OutboxEventEntity, CompletableFuture<SendResult<String, Object>>> entry : inFlight.entrySet()) {
            OutboxEventEntity event = entry.getKey();
            try {
                entry.getValue().join();
                event.markSent();
            } catch (CompletionException ex) {
                log.warn("No se pudo publicar el evento outbox {} (intento {}): {}",
                        event.getId(), event.getAttempts() + 1, ex.getCause().getMessage());
                event.registerFailedAttempt(ex.getCause().getMessage());
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
