package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.util.List;

public record DeviceListResponse(List<DeviceResponse> devices) {
}
