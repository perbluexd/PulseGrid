package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.CreateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.in.CreateDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.CreateDeviceGroupResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CreateDeviceGroupService implements CreateDeviceGroupUseCase {
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Override
    public CreateDeviceGroupResult create(CreateDeviceGroupCommand command) {
        DeviceGroup deviceGroup = new DeviceGroup(UUID.randomUUID(), command.name(), command.description());
        deviceGroupRepositoryPort.save(deviceGroup);

        return new CreateDeviceGroupResult(deviceGroup.getId(), deviceGroup.getName(), deviceGroup.getDescription());
    }
}
