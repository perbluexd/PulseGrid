package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;

public interface GetDeviceGroupUseCase {
    GetDeviceGroupResult get(GetDeviceGroupCommand command);
}
