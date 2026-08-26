package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.ReactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.ReactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.Device;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ReactivateDeviceService implements ReactivateDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public void reactivate(ReactivateDeviceCommand command) {
        Device device = deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));
        device.activate();
        deviceRepositoryPort.save(device);
    }
}
