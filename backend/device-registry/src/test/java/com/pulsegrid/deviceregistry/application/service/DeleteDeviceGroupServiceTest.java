package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DeleteDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotEmptyException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteDeviceGroupServiceTest {

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @Mock
    private DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @InjectMocks
    private DeleteDeviceGroupService deleteDeviceGroupService;

    @Test
    void shouldDeleteGroupWhenItExistsAndIsEmpty() {
        UUID groupId = UUID.randomUUID();
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(deviceGroupMembershipRepositoryPort.existsByGroupId(groupId)).thenReturn(false);

        deleteDeviceGroupService.delete(new DeleteDeviceGroupCommand(groupId));

        verify(deviceGroupRepositoryPort).deleteById(groupId);
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteDeviceGroupService.delete(new DeleteDeviceGroupCommand(groupId)))
                .isInstanceOf(DeviceGroupNotFoundException.class);

        verify(deviceGroupRepositoryPort, never()).deleteById(groupId);
    }

    @Test
    void shouldThrowWhenGroupIsNotEmpty() {
        UUID groupId = UUID.randomUUID();
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));
        when(deviceGroupMembershipRepositoryPort.existsByGroupId(groupId)).thenReturn(true);

        assertThatThrownBy(() -> deleteDeviceGroupService.delete(new DeleteDeviceGroupCommand(groupId)))
                .isInstanceOf(DeviceGroupNotEmptyException.class);

        verify(deviceGroupRepositoryPort, never()).deleteById(groupId);
    }
}
