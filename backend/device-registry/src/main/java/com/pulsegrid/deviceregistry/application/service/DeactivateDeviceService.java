package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DeactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.DeactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.Device;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DeactivateDeviceService implements DeactivateDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public void deactivate(DeactivateDeviceCommand command) {
        Device device = deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));
        device.deactivate();
        deviceRepositoryPort.save(device);
    }
}
