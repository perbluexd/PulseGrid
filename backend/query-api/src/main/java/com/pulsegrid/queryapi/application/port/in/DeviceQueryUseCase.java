package com.pulsegrid.queryapi.application.port.in;

import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DeviceQueryUseCase {
    Optional<DeviceView> getDevice(UUID deviceId);

    List<DeviceView> listDevices(DeviceStatus status);

    List<DeviceGroupView> listGroups();

    Map<UUID, DeviceView> getDevicesByIds(Collection<UUID> deviceIds);

    Map<UUID, DeviceGroupView> getGroupsByIds(Collection<UUID> groupIds);
}
