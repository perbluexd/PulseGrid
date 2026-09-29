package com.pulsegrid.deviceregistry.infrastructure.persistence.repository;

import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DeviceGroupMembershipJpaRepository extends JpaRepository<DeviceGroupMembershipEntity, UUID> {
    boolean existsByDeviceIdAndGroupId(UUID deviceId, UUID groupId);
    List<DeviceGroupMembershipEntity> findByDeviceId(UUID deviceId);
    @Transactional
    void deleteByDeviceIdAndGroupId(UUID deviceId, UUID groupId);
    boolean existsByGroupId(UUID groupId);
}
