package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.EvaluateTelemetryReadingCommand;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.application.port.out.AlertNotificationPublisherPort;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceGroupCachePort;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceSummary;
import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateTelemetryReadingServiceTest {

    @Mock
    private AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Mock
    private AlertRepositoryPort alertRepositoryPort;

    @Mock
    private DeviceGroupCachePort deviceGroupCachePort;

    @Mock
    private DeviceRegistryPort deviceRegistryPort;

    @Mock
    private AlertNotificationPublisherPort alertNotificationPublisherPort;

    @InjectMocks
    private EvaluateTelemetryReadingService evaluateTelemetryReadingService;

    private final UUID deviceId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();

    @Test
    void shouldCreateAlertAndPublishNotificationWhenGroupRuleIsTriggered() {
        AlertRule rule = AlertRule.create(null, groupId, "TEMPERATURE", AlertCondition.GREATER_THAN, 80,
                AlertSeverity.CRITICAL);
        when(deviceGroupCachePort.get(deviceId)).thenReturn(Optional.of(List.of(groupId)));
        when(alertRuleRepositoryPort.findActiveRules(deviceId, List.of(groupId), "TEMPERATURE")).thenReturn(List.of(rule));
        when(alertRepositoryPort.existsOpenAlert(rule.getId(), deviceId)).thenReturn(false);
        when(deviceRegistryPort.findDevice(deviceId)).thenReturn(Optional.of(new DeviceSummary(deviceId, "Horno 3")));

        evaluateTelemetryReadingService.evaluate(reading(92.5));

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepositoryPort).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getStatus()).isEqualTo(AlertStatus.TRIGGERED);
        assertThat(alertCaptor.getValue().getDeviceId()).isEqualTo(deviceId);

        ArgumentCaptor<AlertTriggeredNotification> notificationCaptor = ArgumentCaptor.forClass(AlertTriggeredNotification.class);
        verify(alertNotificationPublisherPort).publish(notificationCaptor.capture());
        AlertTriggeredNotification notification = notificationCaptor.getValue();
        assertThat(notification.alertId()).isEqualTo(alertCaptor.getValue().getId());
        assertThat(notification.deviceName()).isEqualTo("Horno 3");
        assertThat(notification.triggeredValue()).isEqualTo(92.5);
        assertThat(notification.severity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void shouldPopulateCacheFromDeviceRegistryOnCacheMiss() {
        when(deviceGroupCachePort.get(deviceId)).thenReturn(Optional.empty());
        when(deviceRegistryPort.findGroupIds(deviceId)).thenReturn(List.of(groupId));
        when(alertRuleRepositoryPort.findActiveRules(deviceId, List.of(groupId), "TEMPERATURE")).thenReturn(List.of());

        evaluateTelemetryReadingService.evaluate(reading(92.5));

        verify(deviceGroupCachePort).put(deviceId, List.of(groupId));
        verifyNoInteractions(alertNotificationPublisherPort);
    }

    @Test
    void shouldNotCreateAlertWhenConditionIsNotMet() {
        AlertRule rule = AlertRule.create(deviceId, null, "TEMPERATURE", AlertCondition.GREATER_THAN, 80,
                AlertSeverity.CRITICAL);
        when(deviceGroupCachePort.get(deviceId)).thenReturn(Optional.of(List.of()));
        when(alertRuleRepositoryPort.findActiveRules(deviceId, List.of(), "TEMPERATURE")).thenReturn(List.of(rule));

        evaluateTelemetryReadingService.evaluate(reading(70));

        verify(alertRepositoryPort, never()).save(any());
        verifyNoInteractions(alertNotificationPublisherPort);
    }

    @Test
    void shouldNotCreateDuplicateAlertWhileOneIsStillOpen() {
        AlertRule rule = AlertRule.create(deviceId, null, "TEMPERATURE", AlertCondition.GREATER_THAN, 80,
                AlertSeverity.CRITICAL);
        when(deviceGroupCachePort.get(deviceId)).thenReturn(Optional.of(List.of()));
        when(alertRuleRepositoryPort.findActiveRules(deviceId, List.of(), "TEMPERATURE")).thenReturn(List.of(rule));
        when(alertRepositoryPort.existsOpenAlert(rule.getId(), deviceId)).thenReturn(true);

        evaluateTelemetryReadingService.evaluate(reading(95));

        verify(alertRepositoryPort, never()).save(any());
        verifyNoInteractions(alertNotificationPublisherPort);
    }

    private EvaluateTelemetryReadingCommand reading(double value) {
        return new EvaluateTelemetryReadingCommand(deviceId, "TEMPERATURE", value, Instant.now());
    }
}
