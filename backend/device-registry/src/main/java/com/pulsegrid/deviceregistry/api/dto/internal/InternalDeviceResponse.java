package com.pulsegrid.deviceregistry.api.dto.internal;

import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;

import java.util.UUID;

public record InternalDeviceResponse(
        UUID deviceId,
        String name,
        DeviceStatus status
) {
}
