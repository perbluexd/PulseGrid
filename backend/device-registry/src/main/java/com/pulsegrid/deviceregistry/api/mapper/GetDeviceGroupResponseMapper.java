package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.GetDeviceGroupResponse;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import org.springframework.stereotype.Component;

@Component
public class GetDeviceGroupResponseMapper {

    public GetDeviceGroupResponse toResponse(GetDeviceGroupResult result) {
        return new GetDeviceGroupResponse(result.id(), result.name(), result.description());
    }
}
