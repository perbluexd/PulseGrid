package com.pulsegrid.alertingservice.application.port.out;

import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepositoryPort {
    Alert save(Alert alert);
    Optional<Alert> findById(UUID id);
    List<Alert> findAll(AlertStatus status, AlertSeverity severity, UUID deviceId);
    boolean existsOpenAlert(UUID alertRuleId, UUID deviceId);
}
