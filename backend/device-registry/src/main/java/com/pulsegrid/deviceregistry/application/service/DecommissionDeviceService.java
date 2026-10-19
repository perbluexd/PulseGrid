package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.event.DeviceStatusChangedEvent;
import com.pulsegrid.deviceregistry.application.port.in.DecommissionDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.Device;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@AllArgsConstructor
@Service
public class DecommissionDeviceService implements DecommissionDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;
    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private final DeviceRegistryEventPublisherPort deviceRegistryEventPublisherPort;

    @Override
    @Transactional
    public void decommission(DecommissionDeviceCommand command) {
        Device device = deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));
        device.decommission();
        deviceRepositoryPort.save(device);

        String activeApiKeyHash = apiKeyRepositoryPort.findActiveByDeviceId(device.getId())
                .map(ApiKey::getKeyHash)
                .orElse(null);
        deviceRegistryEventPublisherPort.publishDeviceStatusChanged(new DeviceStatusChangedEvent(
                device.getId(),
                device.getStatus(),
                activeApiKeyHash,
                Instant.now()
        ));
    }
}
