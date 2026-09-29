package com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    @Column(nullable = false)
    private UUID id;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "last_error")
    private String lastError;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "sent_at")
    private Instant sentAt;

    public static OutboxEventEntity pending(UUID aggregateId, String eventType, String payload) {
        return new OutboxEventEntity(UUID.randomUUID(), aggregateId, eventType, payload,
                OutboxEventStatus.PENDING, 0, null, Instant.now(), null);
    }

    public void markSent() {
        this.status = OutboxEventStatus.SENT;
        this.sentAt = Instant.now();
        this.lastError = null;
    }

    public void registerFailedAttempt(String error) {
        this.attempts++;
        this.lastError = error;
    }

    public void markFailed(String error) {
        registerFailedAttempt(error);
        this.status = OutboxEventStatus.FAILED;
    }
}
