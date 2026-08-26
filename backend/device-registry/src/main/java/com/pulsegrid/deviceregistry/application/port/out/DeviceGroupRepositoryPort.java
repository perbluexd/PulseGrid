package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceGroupRepositoryPort {
    DeviceGroup save(DeviceGroup deviceGroup);
    Optional<DeviceGroup> findById(UUID id);
    List<DeviceGroup> findAll();
    void deleteById(UUID id);
}
