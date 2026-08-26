package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RegisterDeviceCommand;
import com.pulsegrid.deviceregistry.application.port.in.RegisterDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.RegisterDeviceResult;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RegisterDeviceService implements RegisterDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;
    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private final ApiKeyGeneratorPort apiKeyGeneratorPort;

    @Override
    public RegisterDeviceResult register(RegisterDeviceCommand command) {
        Instant now = Instant.now();
        Device device = new Device(
                UUID.randomUUID(),
                command.name(),
                command.type(),
                DeviceStatus.ACTIVE,
                command.location(),
                now,
                now,
                null
        );
        deviceRepositoryPort.save(device);

        String rawApiKey = apiKeyGeneratorPort.generateRawKey();
        ApiKey apiKey = new ApiKey(
                UUID.randomUUID(),
                device.getId(),
                apiKeyGeneratorPort.hash(rawApiKey),
                ApiKeyStatus.ACTIVE,
                now,
                null,
                null
        );
        apiKeyRepositoryPort.save(apiKey);

        return new RegisterDeviceResult(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getStatus(),
                rawApiKey
        );
    }
}
