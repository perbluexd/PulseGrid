package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import java.time.Instant;
import java.util.UUID;

public record AddDeviceToGroupResponse(
        UUID membershipId,
        UUID deviceId,
        UUID groupId,
        Instant createdAt
) {
}
