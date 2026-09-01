package com.pulsegrid.deviceregistry.api.dto.device;

import java.util.UUID;

public record RegisterDeviceResponse(
        UUID deviceId,
        String name,
        String type,
        String status,
        String apiKey
) {
}
