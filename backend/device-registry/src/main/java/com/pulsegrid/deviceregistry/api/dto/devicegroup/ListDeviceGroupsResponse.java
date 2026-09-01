package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import java.util.List;

public record ListDeviceGroupsResponse(List<GetDeviceGroupResponse> groups) {
}
