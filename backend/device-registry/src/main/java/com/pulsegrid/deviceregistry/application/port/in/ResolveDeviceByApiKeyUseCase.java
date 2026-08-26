package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.port.result.ResolveDeviceByApiKeyResult;

public interface ResolveDeviceByApiKeyUseCase {
    ResolveDeviceByApiKeyResult resolve(ResolveDeviceByApiKeyCommand command);
}
