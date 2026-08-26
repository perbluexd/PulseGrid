package com.pulsegrid.deviceregistry.infrastructure.persistence.repository;

import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DashboardUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DashboardUserJpaRepository extends JpaRepository<DashboardUserEntity, UUID> {
    Optional<DashboardUserEntity> findByEmail(String email);
}
