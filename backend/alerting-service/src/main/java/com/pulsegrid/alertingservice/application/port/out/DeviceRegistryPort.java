package com.pulsegrid.alertingservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRegistryPort {
    Optional<DeviceSummary> findDevice(UUID deviceId);
    boolean groupExists(UUID groupId);
    List<UUID> findGroupIds(UUID deviceId);
}
