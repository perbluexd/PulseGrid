package com.pulsegrid.deviceregistry.application.port.result;

import java.util.List;
import java.util.UUID;

public record GetDeviceGroupsResult(UUID deviceId, List<UUID> groupIds) {
}
