package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.UpdateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDeviceGroupServiceTest {

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @InjectMocks
    private UpdateDeviceGroupService updateDeviceGroupService;

    @Test
    void shouldUpdateDeviceGroupWhenItExists() {
        UUID groupId = UUID.randomUUID();
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        GetDeviceGroupResult result = updateDeviceGroupService.update(
                new UpdateDeviceGroupCommand(groupId, "Lab Sensors Updated", "Updated description"));

        assertThat(result.name()).isEqualTo("Lab Sensors Updated");
        assertThat(result.description()).isEqualTo("Updated description");
        assertThat(group.getName()).isEqualTo("Lab Sensors Updated");
        verify(deviceGroupRepositoryPort).save(group);
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateDeviceGroupService.update(
                new UpdateDeviceGroupCommand(groupId, "New name", "New description")))
                .isInstanceOf(DeviceGroupNotFoundException.class);

        verify(deviceGroupRepositoryPort, never()).save(any());
    }
}
