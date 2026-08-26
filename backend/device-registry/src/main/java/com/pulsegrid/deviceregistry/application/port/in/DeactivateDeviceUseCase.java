package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.DeactivateDeviceCommand;

public interface DeactivateDeviceUseCase {
    void deactivate(DeactivateDeviceCommand command);
}
