package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.UpdateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;

public interface UpdateDeviceGroupUseCase {
    GetDeviceGroupResult update(UpdateDeviceGroupCommand command);
}
