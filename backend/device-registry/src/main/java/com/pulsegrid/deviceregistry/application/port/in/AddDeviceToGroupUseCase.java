package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.AddDeviceToGroupCommand;
import com.pulsegrid.deviceregistry.application.port.result.AddDeviceToGroupResult;

public interface AddDeviceToGroupUseCase {
    AddDeviceToGroupResult addToGroup(AddDeviceToGroupCommand command);
}
