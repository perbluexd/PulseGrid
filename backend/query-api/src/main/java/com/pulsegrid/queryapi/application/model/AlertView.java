package com.pulsegrid.queryapi.application.model;

import java.time.Instant;
import java.util.UUID;

public record AlertView(UUID id, UUID ruleId, UUID deviceId, double triggeredValue, AlertSeverity severity,
                        AlertStatus status, Instant triggeredAt, Instant resolvedAt) {
}
