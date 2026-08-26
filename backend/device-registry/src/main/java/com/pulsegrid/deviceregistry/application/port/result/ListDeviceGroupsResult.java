package com.pulsegrid.deviceregistry.application.port.result;

import java.util.List;

public record ListDeviceGroupsResult(List<GetDeviceGroupResult> groups) {
}
