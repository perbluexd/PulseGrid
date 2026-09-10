package com.pulsegrid.deviceregistry.application.event;

import java.time.Instant;
import java.util.UUID;

public record DeviceGroupMembershipChangedEvent(
        UUID deviceId,
        UUID groupId,
        MembershipChangeType changeType,
        Instant occurredAt
) {
}
