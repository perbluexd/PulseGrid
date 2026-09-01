package com.pulsegrid.deviceregistry.api.dto.internal;

import java.util.UUID;

public record ResolveByApiKeyResponse(
        UUID deviceId,
        String name,
        String type,
        String status
) {
}
