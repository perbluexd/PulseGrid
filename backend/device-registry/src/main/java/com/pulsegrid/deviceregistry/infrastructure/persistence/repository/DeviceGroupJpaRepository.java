package com.pulsegrid.deviceregistry.infrastructure.persistence.repository;

import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceGroupJpaRepository extends JpaRepository<DeviceGroupEntity, UUID> {
}
