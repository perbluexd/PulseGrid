package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.AddDeviceToGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceAlreadyInGroupException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.event.MembershipChangeType;
import com.pulsegrid.deviceregistry.application.port.in.AddDeviceToGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.AddDeviceToGroupResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroupMembership;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Service
public class AddDeviceToGroupService implements AddDeviceToGroupUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;
    private final DeviceGroupRepositoryPort deviceGroupRepositoryPort;
    private final DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;
    private final DeviceRegistryEventPublisherPort deviceRegistryEventPublisherPort;

    @Override
    public AddDeviceToGroupResult addToGroup(AddDeviceToGroupCommand command) {
        deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));
        deviceGroupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new DeviceGroupNotFoundException(command.groupId().toString()));

        if (deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(command.deviceId(), command.groupId())) {
            throw new DeviceAlreadyInGroupException();
        }

        DeviceGroupMembership membership = new DeviceGroupMembership(
                UUID.randomUUID(), command.deviceId(), command.groupId(), Instant.now()
        );
        deviceGroupMembershipRepositoryPort.save(membership);

        deviceRegistryEventPublisherPort.publishMembershipChanged(new DeviceGroupMembershipChangedEvent(
                command.deviceId(),
                command.groupId(),
                MembershipChangeType.ADDED,
                Instant.now()
        ));

        return new AddDeviceToGroupResult(
                membership.getId(), membership.getDeviceId(), membership.getGroupId(), membership.getCreatedAt()
        );
    }
}
