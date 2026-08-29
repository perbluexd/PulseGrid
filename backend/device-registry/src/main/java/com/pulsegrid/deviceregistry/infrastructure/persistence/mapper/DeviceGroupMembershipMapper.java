package com.pulsegrid.deviceregistry.infrastructure.persistence.mapper;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroupMembership;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupMembershipEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceGroupMembershipMapper {
    public DeviceGroupMembership toDomain(DeviceGroupMembershipEntity entity){
        return new DeviceGroupMembership(entity.getId(), entity.getDeviceId(), entity.getGroupId(), entity.getCreatedAt());
    }

    public DeviceGroupMembershipEntity toEntity(DeviceGroupMembership membership){
        return new DeviceGroupMembershipEntity(membership.getId(), membership.getDeviceId(), membership.getGroupId(), membership.getCreatedAt());
    }
}
