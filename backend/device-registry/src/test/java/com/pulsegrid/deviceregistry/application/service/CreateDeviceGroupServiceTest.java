package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.CreateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.CreateDeviceGroupResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateDeviceGroupServiceTest {

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @InjectMocks
    private CreateDeviceGroupService createDeviceGroupService;

    @Test
    void shouldCreateDeviceGroup() {
        CreateDeviceGroupCommand command = new CreateDeviceGroupCommand("Lab Sensors", "Sensors in the lab");

        CreateDeviceGroupResult result = createDeviceGroupService.create(command);

        assertThat(result.groupId()).isNotNull();
        assertThat(result.name()).isEqualTo("Lab Sensors");
        assertThat(result.description()).isEqualTo("Sensors in the lab");
        verify(deviceGroupRepositoryPort).save(any());
    }
}
