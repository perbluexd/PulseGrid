package com.pulsegrid.deviceregistry.application.port.result;

import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record GetDeviceResult(UUID id, String name, DeviceType type, DeviceStatus status, String location,
                               Instant createdAt, Instant updatedAt, Instant lastSeenAt) {
}
