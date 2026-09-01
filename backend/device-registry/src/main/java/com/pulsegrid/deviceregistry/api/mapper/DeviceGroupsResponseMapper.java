package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.internal.DeviceGroupsResponse;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupsResult;
import org.springframework.stereotype.Component;

@Component
public class DeviceGroupsResponseMapper {

    public DeviceGroupsResponse toResponse(GetDeviceGroupsResult result) {
        return new DeviceGroupsResponse(result.deviceId(), result.groupIds());
    }
}
