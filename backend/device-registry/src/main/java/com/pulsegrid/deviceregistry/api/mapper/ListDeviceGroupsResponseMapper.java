package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.ListDeviceGroupsResponse;
import com.pulsegrid.deviceregistry.application.port.result.ListDeviceGroupsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListDeviceGroupsResponseMapper {

    private final GetDeviceGroupResponseMapper getDeviceGroupResponseMapper;

    public ListDeviceGroupsResponse toResponse(ListDeviceGroupsResult result) {
        var groups = result.groups().stream()
                .map(getDeviceGroupResponseMapper::toResponse)
                .toList();
        return new ListDeviceGroupsResponse(groups);
    }
}
