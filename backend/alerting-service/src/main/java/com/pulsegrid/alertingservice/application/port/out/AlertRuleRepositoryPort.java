package com.pulsegrid.alertingservice.application.port.out;

import com.pulsegrid.alertingservice.domain.model.AlertRule;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRuleRepositoryPort {
    AlertRule save(AlertRule alertRule);
    Optional<AlertRule> findById(UUID id);
    List<AlertRule> findAll();
    List<AlertRule> findActiveRules(UUID deviceId, Collection<UUID> groupIds, String metricType);
}
