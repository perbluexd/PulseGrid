package com.pulsegrid.deviceregistry.application.command;

import java.util.UUID;

public record AddDeviceToGroupCommand(UUID deviceId, UUID groupId) {
}
