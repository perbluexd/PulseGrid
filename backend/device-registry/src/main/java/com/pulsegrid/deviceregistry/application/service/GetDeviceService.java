package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.GetDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;
import com.pulsegrid.deviceregistry.domain.model.Device;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GetDeviceService implements GetDeviceUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public GetDeviceResult get(GetDeviceCommand command) {
        Device device = deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));

        return new GetDeviceResult(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getStatus(),
                device.getLocation(),
                device.getCreatedAt(),
                device.getUpdatedAt(),
                device.getLastSeenAt()
        );
    }
}
