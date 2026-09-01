package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.device.GetDeviceResponse;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;
import org.springframework.stereotype.Component;

@Component
public class GetDeviceResponseMapper {

    public GetDeviceResponse toResponse(GetDeviceResult result) {
        return new GetDeviceResponse(
                result.id(),
                result.name(),
                result.type().name(),
                result.status().name(),
                result.location(),
                result.createdAt(),
                result.updatedAt(),
                result.lastSeenAt()
        );
    }
}
