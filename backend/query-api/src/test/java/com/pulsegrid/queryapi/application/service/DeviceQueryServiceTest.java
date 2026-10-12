package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.port.out.DeviceRegistryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceQueryServiceTest {

    @Mock
    private DeviceRegistryPort deviceRegistryPort;

    @InjectMocks
    private DeviceQueryService deviceQueryService;

    @Test
    void shouldRejectMissingDeviceIdWithoutCallingDownstream() {
        assertThatThrownBy(() -> deviceQueryService.getDevice(null))
                .isInstanceOf(InvalidQueryArgumentException.class);
        verify(deviceRegistryPort, never()).findDevice(any());
    }

    @Test
    void shouldFilterDevicesByStatus() {
        var active = device(DeviceStatus.ACTIVE);
        var inactive = device(DeviceStatus.INACTIVE);
        when(deviceRegistryPort.findAllDevices()).thenReturn(List.of(active, inactive));

        assertThat(deviceQueryService.listDevices(DeviceStatus.INACTIVE)).containsExactly(inactive);
        assertThat(deviceQueryService.listDevices(null)).containsExactly(active, inactive);
    }

    @Test
    void shouldReturnOnlyTheRequestedDevicesByIdFromOneDownstreamCall() {
        var first = device(DeviceStatus.ACTIVE);
        var second = device(DeviceStatus.ACTIVE);
        when(deviceRegistryPort.findAllDevices()).thenReturn(List.of(first, second));

        var result = deviceQueryService.getDevicesByIds(List.of(second.id()));

        assertThat(result).containsOnlyKeys(second.id());
        verify(deviceRegistryPort).findAllDevices();
    }

    private DeviceView device(DeviceStatus status) {
        return new DeviceView(UUID.randomUUID(), "sensor", "SENSOR", status, null, Instant.parse("2026-10-01T10:00:00Z"), null);
    }
}
