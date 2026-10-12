package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.util.UUID;

public record DeviceGroupResponse(UUID id, String name, String description) {
}
