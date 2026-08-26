package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.port.in.ListDevicesUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;
import com.pulsegrid.deviceregistry.application.port.result.ListDevicesResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ListDevicesService implements ListDevicesUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public ListDevicesResult listAll() {
        List<GetDeviceResult> devices = deviceRepositoryPort.findAll().stream()
                .map(device -> new GetDeviceResult(
                        device.getId(),
                        device.getName(),
                        device.getType(),
                        device.getStatus(),
                        device.getLocation(),
                        device.getCreatedAt(),
                        device.getUpdatedAt(),
                        device.getLastSeenAt()
                ))
                .toList();

        return new ListDevicesResult(devices);
    }
}
