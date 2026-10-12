package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import com.pulsegrid.queryapi.application.port.out.DeviceRegistryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DeviceQueryService implements DeviceQueryUseCase {

    private final DeviceRegistryPort deviceRegistryPort;

    @Override
    public Optional<DeviceView> getDevice(UUID deviceId) {
        if (deviceId == null) {
            throw new InvalidQueryArgumentException("'id' no es un UUID válido");
        }
        return deviceRegistryPort.findDevice(deviceId);
    }

    @Override
    public List<DeviceView> listDevices(DeviceStatus status) {
        return deviceRegistryPort.findAllDevices().stream()
                .filter(device -> status == null || device.status() == status)
                .toList();
    }

    @Override
    public List<DeviceGroupView> listGroups() {
        return deviceRegistryPort.findAllGroups();
    }

    @Override
    public Map<UUID, DeviceView> getDevicesByIds(Collection<UUID> deviceIds) {
        return deviceRegistryPort.findAllDevices().stream()
                .filter(device -> deviceIds.contains(device.id()))
                .collect(Collectors.toMap(DeviceView::id, Function.identity()));
    }

    @Override
    public Map<UUID, DeviceGroupView> getGroupsByIds(Collection<UUID> groupIds) {
        return deviceRegistryPort.findAllGroups().stream()
                .filter(group -> groupIds.contains(group.id()))
                .collect(Collectors.toMap(DeviceGroupView::id, Function.identity()));
    }
}
