package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.device.RotateApiKeyResponse;
import com.pulsegrid.deviceregistry.application.port.result.RotateDeviceApiKeyResult;
import org.springframework.stereotype.Component;

@Component
public class RotateApiKeyResponseMapper {

    public RotateApiKeyResponse toResponse(RotateDeviceApiKeyResult result) {
        return new RotateApiKeyResponse(
                result.deviceId(),
                result.revokedApiKeyId(),
                result.newApiKeyId(),
                result.newApiKey()
        );
    }
}
