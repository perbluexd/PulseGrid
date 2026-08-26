package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.RotateDeviceApiKeyCommand;
import com.pulsegrid.deviceregistry.application.port.result.RotateDeviceApiKeyResult;

public interface RotateDeviceApiKeyUseCase {
    RotateDeviceApiKeyResult rotate(RotateDeviceApiKeyCommand command);
}
