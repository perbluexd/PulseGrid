package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.AddDeviceToGroupResponse;
import com.pulsegrid.deviceregistry.application.port.result.AddDeviceToGroupResult;
import org.springframework.stereotype.Component;

@Component
public class AddDeviceToGroupResponseMapper {

    public AddDeviceToGroupResponse toResponse(AddDeviceToGroupResult result) {
        return new AddDeviceToGroupResponse(
                result.membershipId(),
                result.deviceId(),
                result.groupId(),
                result.createdAt()
        );
    }
}
