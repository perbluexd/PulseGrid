package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroupMembership;

import java.util.List;
import java.util.UUID;

public interface DeviceGroupMembershipRepositoryPort {
    DeviceGroupMembership save(DeviceGroupMembership membership);
    boolean existsByDeviceIdAndGroupId(UUID deviceId, UUID groupId);
    List<UUID> findGroupIdsByDeviceId(UUID deviceId);
    void deleteByDeviceIdAndGroupId(UUID deviceId, UUID groupId);
    boolean existsByGroupId(UUID groupId);
}
