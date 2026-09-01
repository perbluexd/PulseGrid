package com.pulsegrid.deviceregistry.api.dto.device;

import java.util.UUID;

public record RotateApiKeyResponse(
        UUID deviceId,
        UUID revokedApiKeyId,
        UUID newApiKeyId,
        String newApiKey
) {
}
