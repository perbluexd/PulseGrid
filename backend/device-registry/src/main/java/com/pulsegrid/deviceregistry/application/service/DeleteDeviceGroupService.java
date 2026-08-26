package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DeleteDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotEmptyException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.DeleteDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DeleteDeviceGroupService implements DeleteDeviceGroupUseCase {
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;
    private final DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @Override
    public void delete(DeleteDeviceGroupCommand command) {
        deviceGroupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new DeviceGroupNotFoundException(command.groupId().toString()));

        if (deviceGroupMembershipRepositoryPort.existsByGroupId(command.groupId())) {
            throw new DeviceGroupNotEmptyException();
        }

        deviceGroupRepositoryPort.deleteById(command.groupId());
    }
}
