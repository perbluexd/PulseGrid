package com.pulsegrid.alertingservice.infrastructure.persistence.entity;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alert_rules")
public class AlertRuleEntity {

    @Id
    @Column(nullable = false)
    private UUID id;
    @Column(name = "device_id")
    private UUID deviceId;
    @Column(name = "group_id")
    private UUID groupId;
    @Column(name = "metric_type", nullable = false)
    private String metricType;
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_condition", nullable = false)
    private AlertCondition condition;
    @Column(nullable = false)
    private double threshold;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
