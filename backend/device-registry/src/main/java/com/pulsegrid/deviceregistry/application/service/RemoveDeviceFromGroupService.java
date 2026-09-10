package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotInGroupException;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.event.MembershipChangeType;
import com.pulsegrid.deviceregistry.application.port.in.RemoveDeviceFromGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@AllArgsConstructor
@Service
public class RemoveDeviceFromGroupService implements RemoveDeviceFromGroupUseCase {
    private final DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;
    private final DeviceRegistryEventPublisherPort deviceRegistryEventPublisherPort;

    @Override
    public void remove(RemoveDeviceFromGroupCommand command) {
        if (!deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(command.deviceId(), command.groupId())) {
            throw new DeviceNotInGroupException();
        }
        deviceGroupMembershipRepositoryPort.deleteByDeviceIdAndGroupId(command.deviceId(), command.groupId());

        deviceRegistryEventPublisherPort.publishMembershipChanged(new DeviceGroupMembershipChangedEvent(
                command.deviceId(),
                command.groupId(),
                MembershipChangeType.REMOVED,
                Instant.now()
        ));
    }
}
