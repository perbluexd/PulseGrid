package com.pulsegrid.deviceregistry.api.dto.internal;

import java.util.UUID;

public record InternalDeviceGroupResponse(
        UUID groupId,
        String name
) {
}
