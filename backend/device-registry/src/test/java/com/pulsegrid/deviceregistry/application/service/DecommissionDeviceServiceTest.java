package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.Device;
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
class DecommissionDeviceServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @InjectMocks
    private DecommissionDeviceService decommissionDeviceService;

    @Test
    void shouldDecommissionDeviceWhenItExists() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        decommissionDeviceService.decommission(new DecommissionDeviceCommand(deviceId));

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.DECOMMISSIONED);
        verify(deviceRepositoryPort).save(device);
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decommissionDeviceService.decommission(new DecommissionDeviceCommand(deviceId)))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepositoryPort, never()).save(any());
    }
}
