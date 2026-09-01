package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceRequest;
import com.pulsegrid.deviceregistry.application.command.RegisterDeviceCommand;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.springframework.stereotype.Component;

@Component
public class RegisterDeviceRequestMapper {

    public RegisterDeviceCommand toCommand(RegisterDeviceRequest request) {
        DeviceType type = parseType(request.type());
        return new RegisterDeviceCommand(request.name(), type, request.location());
    }

    private DeviceType parseType(String rawType) {
        try {
            return DeviceType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de dispositivo inválido: " + rawType);
        }
    }
}
