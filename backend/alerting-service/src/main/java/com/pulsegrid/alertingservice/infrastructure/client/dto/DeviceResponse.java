package com.pulsegrid.alertingservice.infrastructure.client.dto;

import java.util.UUID;

public record DeviceResponse(UUID deviceId, String name, String status) {
}
