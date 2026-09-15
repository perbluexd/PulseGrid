package com.pulsegrid.ingestiongateway.application.port.in;

import com.pulsegrid.ingestiongateway.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.ingestiongateway.application.port.result.ResolveDeviceByApiKeyResult;
import reactor.core.publisher.Mono;

public interface ResolveDeviceByApiKeyUseCase {
    Mono<ResolveDeviceByApiKeyResult> resolve(ResolveDeviceByApiKeyCommand command);
}
