package com.pulsegrid.deviceregistry.api.dto.auth;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        UUID userId,
        String email,
        String role
) {
}
