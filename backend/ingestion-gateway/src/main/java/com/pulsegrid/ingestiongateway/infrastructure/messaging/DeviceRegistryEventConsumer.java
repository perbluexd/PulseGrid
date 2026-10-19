package com.pulsegrid.ingestiongateway.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceApiKeyCachePort;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRegistryEventConsumer {

    private static final String REVOKED_API_KEY_HASH_FIELD = "revokedApiKeyHash";
    private static final String ACTIVE_API_KEY_HASH_FIELD = "activeApiKeyHash";

    private final KafkaReceiver<String, String> deviceRegistryEventsReceiver;
    private final DeviceApiKeyCachePort deviceApiKeyCachePort;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    @EventListener(ApplicationReadyEvent.class)
    public void startConsuming() {
        subscription = deviceRegistryEventsReceiver.receive()
                .flatMap(this::handleRecord)
                .subscribe();
    }

    @PreDestroy
    public void stopConsuming() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    private Mono<Void> handleRecord(ReceiverRecord<String, String> record) {
        return Mono.fromCallable(() -> objectMapper.readTree(record.value()))
                .flatMap(this::invalidateCacheIfApiKeyNoLongerValid)
                .doOnError(e -> log.error("Error procesando evento de device-registry-events, offset {}",
                        record.receiverOffset(), e))
                .onErrorResume(e -> Mono.empty())
                .doFinally(signal -> record.receiverOffset().acknowledge());
    }

    private Mono<Void> invalidateCacheIfApiKeyNoLongerValid(JsonNode json) {
        if (json.hasNonNull(REVOKED_API_KEY_HASH_FIELD)) {
            return deviceApiKeyCachePort.evict(json.get(REVOKED_API_KEY_HASH_FIELD).asText());
        }
        if (json.hasNonNull(ACTIVE_API_KEY_HASH_FIELD)) {
            return deviceApiKeyCachePort.evict(json.get(ACTIVE_API_KEY_HASH_FIELD).asText());
        }
        return Mono.empty();
    }
}
