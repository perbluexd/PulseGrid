package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertRuleResponse(UUID alertRuleId, UUID deviceId, UUID groupId, String metricType, String condition,
                                double threshold, String severity, boolean active, Instant createdAt) {
}
