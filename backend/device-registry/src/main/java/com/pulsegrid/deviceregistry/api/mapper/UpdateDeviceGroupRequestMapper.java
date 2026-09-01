package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.UpdateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.application.command.UpdateDeviceGroupCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateDeviceGroupRequestMapper {

    public UpdateDeviceGroupCommand toCommand(UUID groupId, UpdateDeviceGroupRequest request) {
        return new UpdateDeviceGroupCommand(groupId, request.name(), request.description());
    }
}
