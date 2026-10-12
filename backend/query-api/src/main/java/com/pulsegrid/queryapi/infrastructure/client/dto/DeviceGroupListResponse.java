package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.util.List;

public record DeviceGroupListResponse(List<DeviceGroupResponse> groups) {
}
