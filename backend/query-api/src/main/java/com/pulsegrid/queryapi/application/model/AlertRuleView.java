package com.pulsegrid.queryapi.application.model;

import java.time.Instant;
import java.util.UUID;

public record AlertRuleView(UUID id, UUID deviceId, UUID groupId, String metricType, String condition,
                            double threshold, AlertSeverity severity, boolean active, Instant createdAt) {
}
