package com.pulsegrid.alertingservice.infrastructure.persistence.repository;

import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AlertRuleJpaRepository extends JpaRepository<AlertRuleEntity, UUID> {

    List<AlertRuleEntity> findByActiveTrueAndMetricTypeAndDeviceId(String metricType, UUID deviceId);

    @Query("""
            SELECT r FROM AlertRuleEntity r
            WHERE r.active = true
              AND r.metricType = :metricType
              AND (r.deviceId = :deviceId OR r.groupId IN :groupIds)
            """)
    List<AlertRuleEntity> findActiveForDeviceOrGroups(@Param("metricType") String metricType,
                                                      @Param("deviceId") UUID deviceId,
                                                      @Param("groupIds") Collection<UUID> groupIds);
}
