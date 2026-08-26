package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;

public interface RemoveDeviceFromGroupUseCase {
    void remove(RemoveDeviceFromGroupCommand command);
}
