package com.pulsegrid.deviceregistry.application.command;

import java.util.UUID;

public record UpdateDeviceGroupCommand(UUID groupId, String name, String description) {
}
