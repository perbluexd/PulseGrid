package com.pulsegrid.alertingservice.infrastructure.messaging.outbox.repository;

import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
            SELECT * FROM outbox_events
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEventEntity> lockNextPendingBatch(@Param("batchSize") int batchSize);

    @Modifying
    @Query("DELETE FROM OutboxEventEntity e WHERE e.status = :status AND e.sentAt < :sentBefore")
    int deleteByStatusAndSentAtBefore(@Param("status") OutboxEventStatus status, @Param("sentBefore") Instant sentBefore);
}
