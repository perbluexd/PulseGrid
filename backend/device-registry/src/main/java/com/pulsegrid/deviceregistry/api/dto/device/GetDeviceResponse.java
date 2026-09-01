package com.pulsegrid.deviceregistry.api.dto.device;

import java.time.Instant;
import java.util.UUID;

public record GetDeviceResponse(
        UUID id,
        String name,
        String type,
        String status,
        String location,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSeenAt
) {
}
