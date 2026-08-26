package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.port.in.ListDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.application.port.result.ListDeviceGroupsResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ListDeviceGroupsService implements ListDeviceGroupsUseCase {
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Override
    public ListDeviceGroupsResult listAll() {
        List<GetDeviceGroupResult> groups = deviceGroupRepositoryPort.findAll().stream()
                .map(group -> new GetDeviceGroupResult(group.getId(), group.getName(), group.getDescription()))
                .toList();

        return new ListDeviceGroupsResult(groups);
    }
}
