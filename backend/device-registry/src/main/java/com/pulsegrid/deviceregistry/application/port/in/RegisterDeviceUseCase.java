package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.RegisterDeviceCommand;
import com.pulsegrid.deviceregistry.application.port.result.RegisterDeviceResult;

public interface RegisterDeviceUseCase {
    RegisterDeviceResult register(RegisterDeviceCommand command);
}
