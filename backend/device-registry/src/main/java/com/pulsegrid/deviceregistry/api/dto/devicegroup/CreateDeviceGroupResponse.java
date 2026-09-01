package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import java.util.UUID;

public record CreateDeviceGroupResponse(
        UUID groupId,
        String name,
        String description
) {
}
