package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.UpdateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.UpdateDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UpdateDeviceGroupService implements UpdateDeviceGroupUseCase {
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Override
    public GetDeviceGroupResult update(UpdateDeviceGroupCommand command) {
        DeviceGroup deviceGroup = deviceGroupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new DeviceGroupNotFoundException(command.groupId().toString()));

        deviceGroup.updateDetails(command.name(), command.description());
        deviceGroupRepositoryPort.save(deviceGroup);

        return new GetDeviceGroupResult(deviceGroup.getId(), deviceGroup.getName(), deviceGroup.getDescription());
    }
}
