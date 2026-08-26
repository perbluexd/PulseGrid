package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.ApiKey;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepositoryPort {
    ApiKey save(ApiKey apiKey);
    Optional<ApiKey> findByHash(String keyHash);
    Optional<ApiKey> findActiveByDeviceId(UUID deviceId);
}
