package com.pulsegrid.deviceregistry.application.port.result;

import java.util.UUID;

public record RotateDeviceApiKeyResult(UUID deviceId, UUID revokedApiKeyId, UUID newApiKeyId, String newApiKey) {
}
