package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.AddDeviceToGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceAlreadyInGroupException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.AddDeviceToGroupResult;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddDeviceToGroupServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Mock
    private DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @InjectMocks
    private AddDeviceToGroupService addDeviceToGroupService;

    @Test
    void shouldAddDeviceToGroupWhenNotAlreadyMember() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(deviceId, groupId)).thenReturn(false);

        AddDeviceToGroupResult result = addDeviceToGroupService.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId));

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.groupId()).isEqualTo(groupId);
        verify(deviceGroupMembershipRepositoryPort).save(any());
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addDeviceToGroupService.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId)))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceGroupMembershipRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addDeviceToGroupService.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId)))
                .isInstanceOf(DeviceGroupNotFoundException.class);

        verify(deviceGroupMembershipRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenDeviceAlreadyInGroup() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(deviceId, groupId)).thenReturn(true);

        assertThatThrownBy(() -> addDeviceToGroupService.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId)))
                .isInstanceOf(DeviceAlreadyInGroupException.class);

        verify(deviceGroupMembershipRepositoryPort, never()).save(any());
    }
}
