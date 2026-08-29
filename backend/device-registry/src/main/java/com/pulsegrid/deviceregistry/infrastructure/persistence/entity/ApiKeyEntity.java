package com.pulsegrid.deviceregistry.infrastructure.persistence.entity;

import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @Column(nullable = false)
    private UUID id;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "expires_at")
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
}
