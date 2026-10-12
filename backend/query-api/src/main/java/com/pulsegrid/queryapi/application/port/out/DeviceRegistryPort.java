package com.pulsegrid.queryapi.application.port.out;

import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRegistryPort {
    Optional<DeviceView> findDevice(UUID deviceId);

    List<DeviceView> findAllDevices();

    List<DeviceGroupView> findAllGroups();
}
