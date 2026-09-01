package com.pulsegrid.deviceregistry.api.dto.internal;

import jakarta.validation.constraints.NotBlank;

public record ResolveByApiKeyRequest(
        @NotBlank(message = "La API key es obligatoria")
        String apiKey
) {
}
