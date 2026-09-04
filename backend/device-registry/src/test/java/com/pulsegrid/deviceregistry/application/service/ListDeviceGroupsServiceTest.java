package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.ListDeviceGroupsResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDeviceGroupsServiceTest {

    @Mock
    private DeviceGroupRepositoryPort deviceGroupRepositoryPort;

    @InjectMocks
    private ListDeviceGroupsService listDeviceGroupsService;

    @Test
    void shouldReturnAllDeviceGroups() {
        DeviceGroup group1 = new DeviceGroup(UUID.randomUUID(), "Lab Sensors", "Sensors in the lab");
        DeviceGroup group2 = new DeviceGroup(UUID.randomUUID(), "Warehouse Sensors", "Sensors in the warehouse");

        when(deviceGroupRepositoryPort.findAll()).thenReturn(List.of(group1, group2));

        ListDeviceGroupsResult result = listDeviceGroupsService.listAll();

        assertThat(result.groups()).hasSize(2);
        assertThat(result.groups().get(0).name()).isEqualTo("Lab Sensors");
        assertThat(result.groups().get(1).name()).isEqualTo("Warehouse Sensors");
    }

    @Test
    void shouldReturnEmptyListWhenNoGroupsExist() {
        when(deviceGroupRepositoryPort.findAll()).thenReturn(List.of());

        ListDeviceGroupsResult result = listDeviceGroupsService.listAll();

        assertThat(result.groups()).isEmpty();
    }
}
