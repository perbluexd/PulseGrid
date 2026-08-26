package com.pulsegrid.deviceregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApiKey {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID deviceId;
    private final String keyHash;
    private ApiKeyStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Instant revokedAt;

    public ApiKey(UUID id, UUID deviceId, String keyHash, ApiKeyStatus status,
                  Instant createdAt, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.keyHash = keyHash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public void revoke(){
        this.status = ApiKeyStatus.REVOKED;
        this.revokedAt = Instant.now();
    }
    public boolean isActive(){
        return status == ApiKeyStatus.ACTIVE;
    }
}
