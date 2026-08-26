package com.pulsegrid.deviceregistry.application.port.result;

import java.util.UUID;

public record GetDeviceGroupResult(UUID id, String name, String description) {
}
