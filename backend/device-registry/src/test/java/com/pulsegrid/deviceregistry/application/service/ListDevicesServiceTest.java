package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.ListDevicesResult;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDevicesServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @InjectMocks
    private ListDevicesService listDevicesService;

    @Test
    void shouldReturnAllDevices() {
        Device device1 = new Device(UUID.randomUUID(), "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);
        Device device2 = new Device(UUID.randomUUID(), "Sensor 2", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Warehouse", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findAll()).thenReturn(List.of(device1, device2));

        ListDevicesResult result = listDevicesService.listAll();

        assertThat(result.devices()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesExist() {
        when(deviceRepositoryPort.findAll()).thenReturn(List.of());

        ListDevicesResult result = listDevicesService.listAll();

        assertThat(result.devices()).isEmpty();
    }
}
