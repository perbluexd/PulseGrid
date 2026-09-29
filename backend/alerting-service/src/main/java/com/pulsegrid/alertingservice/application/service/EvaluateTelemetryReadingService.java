package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.EvaluateTelemetryReadingCommand;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.application.port.in.EvaluateTelemetryReadingUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertNotificationPublisherPort;
import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceGroupCachePort;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceSummary;
import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class EvaluateTelemetryReadingService implements EvaluateTelemetryReadingUseCase {

    private static final String UNKNOWN_DEVICE_NAME = "Dispositivo desconocido";

    private final AlertRuleRepositoryPort alertRuleRepositoryPort;
    private final AlertRepositoryPort alertRepositoryPort;
    private final DeviceGroupCachePort deviceGroupCachePort;
    private final DeviceRegistryPort deviceRegistryPort;
    private final AlertNotificationPublisherPort alertNotificationPublisherPort;

    @Override
    @Transactional
    public void evaluate(EvaluateTelemetryReadingCommand command) {
        List<UUID> groupIds = resolveGroupIds(command.deviceId());

        List<AlertRule> triggeredRules = alertRuleRepositoryPort
                .findActiveRules(command.deviceId(), groupIds, command.metricType()).stream()
                .filter(rule -> rule.isTriggeredBy(command.metricType(), command.value()))
                .filter(rule -> !alertRepositoryPort.existsOpenAlert(rule.getId(), command.deviceId()))
                .toList();

        if (triggeredRules.isEmpty()) {
            return;
        }

        String deviceName = deviceRegistryPort.findDevice(command.deviceId())
                .map(DeviceSummary::name)
                .orElse(UNKNOWN_DEVICE_NAME);

        for (AlertRule rule : triggeredRules) {
            Alert alert = Alert.trigger(rule, command.deviceId(), command.value());
            alertRepositoryPort.save(alert);
            alertNotificationPublisherPort.publish(new AlertTriggeredNotification(
                    alert.getId(),
                    rule.getId(),
                    alert.getDeviceId(),
                    deviceName,
                    rule.getMetricType(),
                    rule.getCondition(),
                    rule.getThreshold(),
                    alert.getTriggeredValue(),
                    alert.getSeverity(),
                    alert.getTriggeredAt()
            ));
        }
    }

    private List<UUID> resolveGroupIds(UUID deviceId) {
        return deviceGroupCachePort.get(deviceId).orElseGet(() -> {
            List<UUID> groupIds = deviceRegistryPort.findGroupIds(deviceId);
            deviceGroupCachePort.put(deviceId, groupIds);
            return groupIds;
        });
    }
}
