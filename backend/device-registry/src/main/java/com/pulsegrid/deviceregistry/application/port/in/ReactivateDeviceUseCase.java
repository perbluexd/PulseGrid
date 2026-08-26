package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.ReactivateDeviceCommand;

public interface ReactivateDeviceUseCase {
    void reactivate(ReactivateDeviceCommand command);
}
