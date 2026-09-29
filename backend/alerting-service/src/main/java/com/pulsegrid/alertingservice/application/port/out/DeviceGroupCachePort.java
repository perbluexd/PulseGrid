package com.pulsegrid.alertingservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceGroupCachePort {
    Optional<List<UUID>> get(UUID deviceId);
    void put(UUID deviceId, List<UUID> groupIds);
    void evict(UUID deviceId);
}
