package com.pulsegrid.deviceregistry.application.command;

import java.util.UUID;

public record RemoveDeviceFromGroupCommand(UUID deviceId, UUID groupId) {
}
