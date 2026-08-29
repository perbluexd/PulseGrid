package com.pulsegrid.deviceregistry.infrastructure.persistence.mapper;

import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.ApiKeyEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyMapper {
    public ApiKey toDomain(ApiKeyEntity entity){
        return new ApiKey(entity.getId(), entity.getDeviceId(), entity.getKeyHash(), entity.getStatus(),
                entity.getCreatedAt(), entity.getExpiresAt(), entity.getRevokedAt());
    }

    public ApiKeyEntity toEntity(ApiKey apiKey){
        return new ApiKeyEntity(apiKey.getId(), apiKey.getDeviceId(), apiKey.getKeyHash(), apiKey.getStatus(),
                apiKey.getCreatedAt(), apiKey.getExpiresAt(), apiKey.getRevokedAt());
    }
}
