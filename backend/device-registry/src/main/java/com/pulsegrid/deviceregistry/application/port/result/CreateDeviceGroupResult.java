package com.pulsegrid.deviceregistry.application.port.result;

import java.util.UUID;

public record CreateDeviceGroupResult(UUID groupId, String name, String description) {
}
