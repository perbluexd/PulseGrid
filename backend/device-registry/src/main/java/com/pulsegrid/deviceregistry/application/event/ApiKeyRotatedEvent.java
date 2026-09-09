package com.pulsegrid.deviceregistry.application.event;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyRotatedEvent(
        UUID deviceId,
        UUID revokedApiKeyId,
        UUID newApiKeyId,
        Instant occurredAt
) {
}
