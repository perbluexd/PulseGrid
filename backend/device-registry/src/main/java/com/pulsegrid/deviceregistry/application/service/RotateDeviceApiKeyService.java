package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RotateDeviceApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.ActiveApiKeyNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.port.in.RotateDeviceApiKeyUseCase;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.RotateDeviceApiKeyResult;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RotateDeviceApiKeyService implements RotateDeviceApiKeyUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;
    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private final ApiKeyGeneratorPort apiKeyGeneratorPort;
    private final DeviceRegistryEventPublisherPort deviceRegistryEventPublisherPort;

    @Override
    public RotateDeviceApiKeyResult rotate(RotateDeviceApiKeyCommand command) {
        deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));

        ApiKey currentActiveKey = apiKeyRepositoryPort.findActiveByDeviceId(command.deviceId())
                .orElseThrow(ActiveApiKeyNotFoundException::new);
        currentActiveKey.revoke();
        apiKeyRepositoryPort.save(currentActiveKey);

        String rawApiKey = apiKeyGeneratorPort.generateRawKey();
        ApiKey newApiKey = new ApiKey(
                UUID.randomUUID(),
                command.deviceId(),
                apiKeyGeneratorPort.hash(rawApiKey),
                ApiKeyStatus.ACTIVE,
                Instant.now(),
                null,
                null
        );
        apiKeyRepositoryPort.save(newApiKey);

        deviceRegistryEventPublisherPort.publishApiKeyRotated(new ApiKeyRotatedEvent(
                command.deviceId(),
                currentActiveKey.getId(),
                newApiKey.getId(),
                Instant.now()
        ));

        return new RotateDeviceApiKeyResult(
                command.deviceId(),
                currentActiveKey.getId(),
                newApiKey.getId(),
                rawApiKey
        );
    }
}
