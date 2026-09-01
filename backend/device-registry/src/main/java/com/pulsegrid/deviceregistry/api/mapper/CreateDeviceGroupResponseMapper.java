package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupResponse;
import com.pulsegrid.deviceregistry.application.port.result.CreateDeviceGroupResult;
import org.springframework.stereotype.Component;

@Component
public class CreateDeviceGroupResponseMapper {

    public CreateDeviceGroupResponse toResponse(CreateDeviceGroupResult result) {
        return new CreateDeviceGroupResponse(result.groupId(), result.name(), result.description());
    }
}
