package com.pulsegrid.ingestiongateway.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeviceApiKeyCachePort {
    Mono<UUID> get(String apiKey);

    Mono<Void> put(String apiKey, UUID deviceId);

    Mono<Void> evict(String apiKeyHash);
}
