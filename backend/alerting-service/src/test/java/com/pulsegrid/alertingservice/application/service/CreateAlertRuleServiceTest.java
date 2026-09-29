package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.CreateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.alertingservice.application.error.DeviceNotFoundException;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceSummary;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
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
class CreateAlertRuleServiceTest {

    @Mock
    private AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Mock
    private DeviceRegistryPort deviceRegistryPort;

    @InjectMocks
    private CreateAlertRuleService createAlertRuleService;

    @Test
    void shouldCreateDeviceRuleWhenDeviceExists() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRegistryPort.findDevice(deviceId)).thenReturn(Optional.of(new DeviceSummary(deviceId, "Sensor 1")));

        AlertRuleResult result = createAlertRuleService.create(new CreateAlertRuleCommand(deviceId, null,
                "TEMPERATURE", AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL));

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.active()).isTrue();
        verify(alertRuleRepositoryPort).save(any());
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRegistryPort.findDevice(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createAlertRuleService.create(new CreateAlertRuleCommand(deviceId, null,
                "TEMPERATURE", AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL)))
                .isInstanceOf(DeviceNotFoundException.class);
        verify(alertRuleRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        when(deviceRegistryPort.groupExists(groupId)).thenReturn(false);

        assertThatThrownBy(() -> createAlertRuleService.create(new CreateAlertRuleCommand(null, groupId,
                "HUMIDITY", AlertCondition.LESS_THAN, 20, AlertSeverity.WARNING)))
                .isInstanceOf(DeviceGroupNotFoundException.class);
        verify(alertRuleRepositoryPort, never()).save(any());
    }
}
