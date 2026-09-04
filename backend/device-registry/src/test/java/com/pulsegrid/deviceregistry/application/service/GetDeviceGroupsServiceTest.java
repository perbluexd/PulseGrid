package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupsResult;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDeviceGroupsServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @InjectMocks
    private GetDeviceGroupsService getDeviceGroupsService;

    @Test
    void shouldReturnGroupIdsWhenDeviceExists() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId1 = UUID.randomUUID();
        UUID groupId2 = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceGroupMembershipRepositoryPort.findGroupIdsByDeviceId(deviceId))
                .thenReturn(List.of(groupId1, groupId2));

        GetDeviceGroupsResult result = getDeviceGroupsService.getGroups(new GetDeviceGroupsCommand(deviceId));

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.groupIds()).containsExactly(groupId1, groupId2);
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getDeviceGroupsService.getGroups(new GetDeviceGroupsCommand(deviceId)))
                .isInstanceOf(DeviceNotFoundException.class);
    }
}
