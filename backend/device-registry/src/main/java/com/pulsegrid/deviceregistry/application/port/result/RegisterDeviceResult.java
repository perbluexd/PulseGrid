package com.pulsegrid.deviceregistry.application.port.result;

import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;

import java.util.UUID;

public record RegisterDeviceResult(UUID deviceId, String name, DeviceType type, DeviceStatus status, String apiKey) {
}
