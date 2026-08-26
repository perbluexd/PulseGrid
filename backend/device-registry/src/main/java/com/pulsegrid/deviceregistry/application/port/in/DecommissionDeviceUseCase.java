package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;

public interface DecommissionDeviceUseCase {
    void decommission(DecommissionDeviceCommand command);
}
