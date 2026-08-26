package com.pulsegrid.deviceregistry.application.command;

import com.pulsegrid.deviceregistry.domain.model.DeviceType;

public record RegisterDeviceCommand(String name, DeviceType type, String location) {
}
