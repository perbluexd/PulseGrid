package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.Device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findById(UUID id);
    List<Device> findAll();
}
