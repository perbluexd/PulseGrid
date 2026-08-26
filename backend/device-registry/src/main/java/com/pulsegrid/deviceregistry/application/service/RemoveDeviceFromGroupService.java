package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotInGroupException;
import com.pulsegrid.deviceregistry.application.port.in.RemoveDeviceFromGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RemoveDeviceFromGroupService implements RemoveDeviceFromGroupUseCase {
    private final DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @Override
    public void remove(RemoveDeviceFromGroupCommand command) {
        if (!deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(command.deviceId(), command.groupId())) {
            throw new DeviceNotInGroupException();
        }
        deviceGroupMembershipRepositoryPort.deleteByDeviceIdAndGroupId(command.deviceId(), command.groupId());
    }
}
