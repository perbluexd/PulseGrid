package com.pulsegrid.deviceregistry.application.command;

import java.util.UUID;

public record DeleteDeviceGroupCommand(UUID groupId) {
}
