package com.pulsegrid.deviceregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Device {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final String name;
    private final DeviceType type;
    private DeviceStatus status;
    private final String location;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastSeenAt;

    public Device(UUID id, String name, DeviceType type, DeviceStatus status, String location,
                  Instant createdAt, Instant updatedAt, Instant lastSeenAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastSeenAt = lastSeenAt;
    }

    public void activate(){
        this.status = DeviceStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
    public void deactivate(){
        this.status = DeviceStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
    public void decommission(){
        this.status = DeviceStatus.DECOMMISSIONED;
        this.updatedAt = Instant.now();
    }
    public void recordTelemetryReceived(){
        this.lastSeenAt = Instant.now();
    }
}
