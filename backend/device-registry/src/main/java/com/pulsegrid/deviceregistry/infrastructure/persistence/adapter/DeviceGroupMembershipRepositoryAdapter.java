package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroupMembership;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupMembershipEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceGroupMembershipMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceGroupMembershipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceGroupMembershipRepositoryAdapter implements DeviceGroupMembershipRepositoryPort {
    private final DeviceGroupMembershipJpaRepository repository;
    private final DeviceGroupMembershipMapper mapper;

    @Override
    public DeviceGroupMembership save(DeviceGroupMembership membership){
        DeviceGroupMembershipEntity saved = repository.save(mapper.toEntity(membership));
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByDeviceIdAndGroupId(UUID deviceId, UUID groupId){
        return repository.existsByDeviceIdAndGroupId(deviceId, groupId);
    }

    @Override
    public List<UUID> findGroupIdsByDeviceId(UUID deviceId){
        return repository.findByDeviceId(deviceId).stream()
                .map(DeviceGroupMembershipEntity::getGroupId)
                .toList();
    }

    @Override
    public void deleteByDeviceIdAndGroupId(UUID deviceId, UUID groupId){
        repository.deleteByDeviceIdAndGroupId(deviceId, groupId);
    }

    @Override
    public boolean existsByGroupId(UUID groupId){
        return repository.existsByGroupId(groupId);
    }
}
