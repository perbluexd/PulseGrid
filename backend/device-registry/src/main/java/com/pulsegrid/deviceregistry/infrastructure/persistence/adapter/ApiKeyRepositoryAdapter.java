package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.ApiKeyEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.ApiKeyMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.ApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiKeyRepositoryAdapter implements ApiKeyRepositoryPort {
    private final ApiKeyJpaRepository repository;
    private final ApiKeyMapper mapper;

    @Override
    public ApiKey save(ApiKey apiKey){
        ApiKeyEntity saved = repository.save(mapper.toEntity(apiKey));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ApiKey> findByHash(String keyHash){
        return repository.findByKeyHash(keyHash).map(mapper::toDomain);
    }

    @Override
    public Optional<ApiKey> findActiveByDeviceId(UUID deviceId){
        return repository.findByDeviceIdAndStatus(deviceId, ApiKeyStatus.ACTIVE).map(mapper::toDomain);
    }
}
