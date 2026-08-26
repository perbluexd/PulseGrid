package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GetDeviceGroupService implements GetDeviceGroupUseCase {
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Override
    public GetDeviceGroupResult get(GetDeviceGroupCommand command) {
        DeviceGroup deviceGroup = deviceGroupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new DeviceGroupNotFoundException(command.groupId().toString()));

        return new GetDeviceGroupResult(deviceGroup.getId(), deviceGroup.getName(), deviceGroup.getDescription());
    }
}
