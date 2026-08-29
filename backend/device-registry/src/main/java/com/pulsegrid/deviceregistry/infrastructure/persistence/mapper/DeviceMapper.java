package com.pulsegrid.deviceregistry.infrastructure.persistence.mapper;

import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {
    public Device toDomain(DeviceEntity entity){
        return new Device(entity.getId(), entity.getName(), entity.getType(), entity.getStatus(),
                entity.getLocation(), entity.getCreatedAt(), entity.getUpdatedAt(), entity.getLastSeenAt());
    }

    public DeviceEntity toEntity(Device device){
        return new DeviceEntity(device.getId(), device.getName(), device.getType(), device.getStatus(),
                device.getLocation(), device.getCreatedAt(), device.getUpdatedAt(), device.getLastSeenAt());
    }
}
