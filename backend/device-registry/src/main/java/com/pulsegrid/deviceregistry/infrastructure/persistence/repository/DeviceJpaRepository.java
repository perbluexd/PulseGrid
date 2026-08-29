package com.pulsegrid.deviceregistry.infrastructure.persistence.repository;

import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, UUID> {
}
