package com.pulsegrid.deviceregistry.infrastructure.persistence.mapper;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceGroupMapper {
    public DeviceGroup toDomain(DeviceGroupEntity entity){
        return new DeviceGroup(entity.getId(), entity.getName(), entity.getDescription());
    }

    public DeviceGroupEntity toEntity(DeviceGroup deviceGroup){
        return new DeviceGroupEntity(deviceGroup.getId(), deviceGroup.getName(), deviceGroup.getDescription());
    }
}
