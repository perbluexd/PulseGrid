package com.pulsegrid.notificationworkers.infrastructure.persistence.repository;

import com.pulsegrid.notificationworkers.infrastructure.persistence.entity.ProcessedNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedNotificationJpaRepository extends JpaRepository<ProcessedNotificationEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_notifications (alert_id, processed_at)
            VALUES (:alertId, :processedAt)
            ON CONFLICT (alert_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("alertId") UUID alertId, @Param("processedAt") Instant processedAt);
}
