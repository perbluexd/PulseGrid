package com.pulsegrid.deviceregistry.application.port.result;

import java.time.Instant;
import java.util.UUID;

public record AddDeviceToGroupResult(UUID membershipId, UUID deviceId, UUID groupId, Instant createdAt) {
}
