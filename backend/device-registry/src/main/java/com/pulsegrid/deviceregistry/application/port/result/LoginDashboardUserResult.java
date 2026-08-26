package com.pulsegrid.deviceregistry.application.port.result;

import com.pulsegrid.deviceregistry.domain.model.Role;

import java.util.UUID;

public record LoginDashboardUserResult(String accessToken, long expiresIn, UUID userId, String email, Role role) {
}
