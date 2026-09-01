package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.device.ListDevicesResponse;
import com.pulsegrid.deviceregistry.application.port.result.ListDevicesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListDevicesResponseMapper {

    private final GetDeviceResponseMapper getDeviceResponseMapper;

    public ListDevicesResponse toResponse(ListDevicesResult result) {
        var devices = result.devices().stream()
                .map(getDeviceResponseMapper::toResponse)
                .toList();
        return new ListDevicesResponse(devices);
    }
}
