package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyResponse;
import com.pulsegrid.deviceregistry.application.port.result.ResolveDeviceByApiKeyResult;
import org.springframework.stereotype.Component;

@Component
public class ResolveByApiKeyResponseMapper {

    public ResolveByApiKeyResponse toResponse(ResolveDeviceByApiKeyResult result) {
        return new ResolveByApiKeyResponse(
                result.deviceId(),
                result.name(),
                result.type().name(),
                result.status().name()
        );
    }
}
