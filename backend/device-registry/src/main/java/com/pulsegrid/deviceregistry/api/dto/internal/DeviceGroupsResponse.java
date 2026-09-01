package com.pulsegrid.deviceregistry.api.dto.internal;

import java.util.List;
import java.util.UUID;

public record DeviceGroupsResponse(
        UUID deviceId,
        List<UUID> groupIds
) {
}
