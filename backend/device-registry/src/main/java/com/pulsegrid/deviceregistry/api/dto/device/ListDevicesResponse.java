package com.pulsegrid.deviceregistry.api.dto.device;

import java.util.List;

public record ListDevicesResponse(List<GetDeviceResponse> devices) {
}
