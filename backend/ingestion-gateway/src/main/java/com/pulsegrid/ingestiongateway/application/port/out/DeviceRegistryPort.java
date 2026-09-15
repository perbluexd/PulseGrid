package com.pulsegrid.ingestiongateway.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeviceRegistryPort {
    Mono<UUID> resolveByApiKey(String apiKey);
}
