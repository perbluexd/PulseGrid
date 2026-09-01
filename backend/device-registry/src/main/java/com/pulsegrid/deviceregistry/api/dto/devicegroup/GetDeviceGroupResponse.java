package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import java.util.UUID;

public record GetDeviceGroupResponse(
        UUID id,
        String name,
        String description
) {
}
