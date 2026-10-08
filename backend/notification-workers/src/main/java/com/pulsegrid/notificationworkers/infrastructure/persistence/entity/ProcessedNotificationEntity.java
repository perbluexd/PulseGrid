package com.pulsegrid.notificationworkers.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedNotificationEntity {

    @Id
    @Column(name = "alert_id")
    private UUID alertId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
