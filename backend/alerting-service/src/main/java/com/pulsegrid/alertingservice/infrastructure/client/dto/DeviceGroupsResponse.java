package com.pulsegrid.alertingservice.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

public record DeviceGroupsResponse(UUID deviceId, List<UUID> groupIds) {
}
