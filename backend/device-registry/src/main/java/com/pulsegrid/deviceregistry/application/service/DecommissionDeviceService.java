package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.DecommissionDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.Device;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DecommissionDeviceService implements DecommissionDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public void decommission(DecommissionDeviceCommand command) {
        Device device = deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));
        device.decommission();
        deviceRepositoryPort.save(device);
    }
}
