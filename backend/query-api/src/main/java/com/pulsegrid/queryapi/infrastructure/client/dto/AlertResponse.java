package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(UUID alertId, UUID alertRuleId, UUID deviceId, double triggeredValue, String severity,
                            String status, Instant triggeredAt, Instant resolvedAt) {
}
