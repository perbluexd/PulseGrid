package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.GetDeviceCommand;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;

public interface GetDeviceUseCase {
    GetDeviceResult get(GetDeviceCommand command);
}
