package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.DeleteDeviceGroupCommand;

public interface DeleteDeviceGroupUseCase {
    void delete(DeleteDeviceGroupCommand command);
}
