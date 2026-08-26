package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupsResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class GetDeviceGroupsService implements GetDeviceGroupsUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;
    private final DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @Override
    public GetDeviceGroupsResult getGroups(GetDeviceGroupsCommand command) {
        deviceRepositoryPort.findById(command.deviceId())
                .orElseThrow(() -> new DeviceNotFoundException(command.deviceId().toString()));

        return new GetDeviceGroupsResult(
                command.deviceId(),
                deviceGroupMembershipRepositoryPort.findGroupIdsByDeviceId(command.deviceId())
        );
    }
}
