package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.event.DeviceStatusChangedEvent;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Mock
    private ApiKeyRepositoryPort apiKeyRepositoryPort;

    @Mock
    private DeviceRegistryEventPublisherPort deviceRegistryEventPublisherPort;

    @InjectMocks
    private DecommissionDeviceService decommissionDeviceService;

    @Test
    void shouldDecommissionDeviceWhenItExists() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        ApiKey activeKey = new ApiKey(UUID.randomUUID(), deviceId, "active-hash", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(apiKeyRepositoryPort.findActiveByDeviceId(deviceId)).thenReturn(Optional.of(activeKey));

        decommissionDeviceService.decommission(new DecommissionDeviceCommand(deviceId));

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.DECOMMISSIONED);
        verify(deviceRepositoryPort).save(device);

        ArgumentCaptor<DeviceStatusChangedEvent> captor = ArgumentCaptor.forClass(DeviceStatusChangedEvent.class);
        verify(deviceRegistryEventPublisherPort).publishDeviceStatusChanged(captor.capture());
        assertThat(captor.getValue().deviceId()).isEqualTo(deviceId);
        assertThat(captor.getValue().newStatus()).isEqualTo(DeviceStatus.DECOMMISSIONED);
        assertThat(captor.getValue().activeApiKeyHash()).isEqualTo("active-hash");
    }

    @Test
    void shouldPublishEventWithoutApiKeyHashWhenDeviceHasNoActiveKey() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(apiKeyRepositoryPort.findActiveByDeviceId(deviceId)).thenReturn(Optional.empty());

        decommissionDeviceService.decommission(new DecommissionDeviceCommand(deviceId));

        ArgumentCaptor<DeviceStatusChangedEvent> captor = ArgumentCaptor.forClass(DeviceStatusChangedEvent.class);
        verify(deviceRegistryEventPublisherPort).publishDeviceStatusChanged(captor.capture());
        assertThat(captor.getValue().activeApiKeyHash()).isNull();
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decommissionDeviceService.decommission(new DecommissionDeviceCommand(deviceId)))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepositoryPort, never()).save(any());
        verify(deviceRegistryEventPublisherPort, never()).publishDeviceStatusChanged(any());
    }
}
