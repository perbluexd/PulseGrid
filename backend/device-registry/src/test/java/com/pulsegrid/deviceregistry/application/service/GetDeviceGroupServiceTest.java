package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDeviceGroupServiceTest {

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @InjectMocks
    private GetDeviceGroupService getDeviceGroupService;

    @Test
    void shouldReturnDeviceGroupWhenItExists() {
        UUID groupId = UUID.randomUUID();
        DeviceGroup group = new DeviceGroup(groupId, "Lab Sensors", "Sensors in the lab");

        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.of(group));

        GetDeviceGroupResult result = getDeviceGroupService.get(new GetDeviceGroupCommand(groupId));

        assertThat(result.id()).isEqualTo(groupId);
        assertThat(result.name()).isEqualTo("Lab Sensors");
        assertThat(result.description()).isEqualTo("Sensors in the lab");
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        when(deviceGroupRepositoryPort.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getDeviceGroupService.get(new GetDeviceGroupCommand(groupId)))
                .isInstanceOf(DeviceGroupNotFoundException.class);
    }
}
