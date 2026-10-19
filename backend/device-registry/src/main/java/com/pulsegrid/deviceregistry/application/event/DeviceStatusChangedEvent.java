package com.pulsegrid.deviceregistry.application.event;

import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record DeviceStatusChangedEvent(
        UUID deviceId,
        DeviceStatus newStatus,
        String activeApiKeyHash,
        Instant occurredAt
) {
}
