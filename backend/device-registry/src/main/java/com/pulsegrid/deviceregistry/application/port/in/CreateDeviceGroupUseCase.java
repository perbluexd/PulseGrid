package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.CreateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.result.CreateDeviceGroupResult;

public interface CreateDeviceGroupUseCase {
    CreateDeviceGroupResult create(CreateDeviceGroupCommand command);
}
