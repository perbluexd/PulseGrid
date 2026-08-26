package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotActiveException;
import com.pulsegrid.deviceregistry.application.error.InvalidApiKeyException;
import com.pulsegrid.deviceregistry.application.port.in.ResolveDeviceByApiKeyUseCase;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.ResolveDeviceByApiKeyResult;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ResolveDeviceByApiKeyService implements ResolveDeviceByApiKeyUseCase {
    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private final ApiKeyGeneratorPort apiKeyGeneratorPort;
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public ResolveDeviceByApiKeyResult resolve(ResolveDeviceByApiKeyCommand command) {
        String keyHash = apiKeyGeneratorPort.hash(command.rawApiKey());
        ApiKey apiKey = apiKeyRepositoryPort.findByHash(keyHash)
                .orElseThrow(InvalidApiKeyException::new);

        if (!apiKey.isActive()) {
            throw new InvalidApiKeyException();
        }

        Device device = deviceRepositoryPort.findById(apiKey.getDeviceId())
                .orElseThrow(InvalidApiKeyException::new);

        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new DeviceNotActiveException(device.getId().toString());
        }

        return new ResolveDeviceByApiKeyResult(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getStatus()
        );
    }
}
