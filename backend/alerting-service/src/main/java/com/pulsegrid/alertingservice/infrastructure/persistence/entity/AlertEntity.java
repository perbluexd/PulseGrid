package com.pulsegrid.alertingservice.infrastructure.persistence.entity;

import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;
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
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @Column(nullable = false)
    private UUID id;
    @Column(name = "alert_rule_id", nullable = false)
    private UUID alertRuleId;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @Column(name = "triggered_value", nullable = false)
    private double triggeredValue;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;
    @Column(name = "resolved_by")
    private UUID resolvedBy;
}
