package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(UUID id, String name, String type, String status, String location,
                             Instant createdAt, Instant updatedAt, Instant lastSeenAt) {
}
