package com.pulsegrid.ingestiongateway.application.service;

import com.pulsegrid.ingestiongateway.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.ingestiongateway.application.port.in.ResolveDeviceByApiKeyUseCase;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceApiKeyCachePort;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceRegistryPort;
import com.pulsegrid.ingestiongateway.application.port.result.ResolveDeviceByApiKeyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ResolveDeviceByApiKeyService implements ResolveDeviceByApiKeyUseCase {

    private final DeviceApiKeyCachePort deviceApiKeyCachePort;
    private final DeviceRegistryPort deviceRegistryPort;

    @Override
    public Mono<ResolveDeviceByApiKeyResult> resolve(ResolveDeviceByApiKeyCommand command) {
        return deviceApiKeyCachePort.get(command.apiKey())
                .switchIfEmpty(
                        deviceRegistryPort.resolveByApiKey(command.apiKey())
                                .flatMap(deviceId -> deviceApiKeyCachePort.put(command.apiKey(), deviceId)
                                        .thenReturn(deviceId))
                )
                .map(ResolveDeviceByApiKeyResult::new);
    }
}
