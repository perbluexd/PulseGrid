package com.pulsegrid.alertingservice.infrastructure.persistence.repository;

import com.pulsegrid.alertingservice.domain.model.AlertStatus;
import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID>, JpaSpecificationExecutor<AlertEntity> {

    boolean existsByAlertRuleIdAndDeviceIdAndStatusIn(UUID alertRuleId, UUID deviceId, Collection<AlertStatus> statuses);
}
