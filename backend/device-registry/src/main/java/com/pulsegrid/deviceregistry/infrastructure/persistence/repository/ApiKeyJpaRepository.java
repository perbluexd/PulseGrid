package com.pulsegrid.deviceregistry.infrastructure.persistence.repository;

import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyEntity, UUID> {
    Optional<ApiKeyEntity> findByKeyHash(String keyHash);
    Optional<ApiKeyEntity> findByDeviceIdAndStatus(UUID deviceId, ApiKeyStatus status);
}
