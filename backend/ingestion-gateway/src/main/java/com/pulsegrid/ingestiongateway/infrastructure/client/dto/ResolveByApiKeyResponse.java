package com.pulsegrid.ingestiongateway.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResolveByApiKeyResponse(UUID deviceId) {
}
