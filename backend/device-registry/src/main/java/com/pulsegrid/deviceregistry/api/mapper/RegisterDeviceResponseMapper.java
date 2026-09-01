package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceResponse;
import com.pulsegrid.deviceregistry.application.port.result.RegisterDeviceResult;
import org.springframework.stereotype.Component;

@Component
public class RegisterDeviceResponseMapper {

    public RegisterDeviceResponse toResponse(RegisterDeviceResult result) {
        return new RegisterDeviceResponse(
                result.deviceId(),
                result.name(),
                result.type().name(),
                result.status().name(),
                result.apiKey()
        );
    }
}
