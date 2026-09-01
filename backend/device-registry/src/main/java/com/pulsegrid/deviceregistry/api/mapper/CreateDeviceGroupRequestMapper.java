package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.application.command.CreateDeviceGroupCommand;
import org.springframework.stereotype.Component;

@Component
public class CreateDeviceGroupRequestMapper {

    public CreateDeviceGroupCommand toCommand(CreateDeviceGroupRequest request) {
        return new CreateDeviceGroupCommand(request.name(), request.description());
    }
}
