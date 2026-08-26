package com.pulsegrid.deviceregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceGroupMembership {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final UUID deviceId;
    private final UUID groupId;
    private final Instant createdAt;

    public DeviceGroupMembership(UUID id, UUID deviceId, UUID groupId, Instant createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.groupId = groupId;
        this.createdAt = createdAt;
    }
}
