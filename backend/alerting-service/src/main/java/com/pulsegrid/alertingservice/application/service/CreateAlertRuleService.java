package com.pulsegrid.alertingservice.application.service;

import com.pulsegrid.alertingservice.application.command.CreateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.alertingservice.application.error.DeviceNotFoundException;
import com.pulsegrid.alertingservice.application.port.in.CreateAlertRuleUseCase;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreateAlertRuleService implements CreateAlertRuleUseCase {
    private final AlertRuleRepositoryPort alertRuleRepositoryPort;
    private final DeviceRegistryPort deviceRegistryPort;

    @Override
    public AlertRuleResult create(CreateAlertRuleCommand command) {
        AlertRule alertRule = AlertRule.create(command.deviceId(), command.groupId(), command.metricType(),
                command.condition(), command.threshold(), command.severity());

        if (alertRule.getDeviceId() != null && deviceRegistryPort.findDevice(alertRule.getDeviceId()).isEmpty()) {
            throw new DeviceNotFoundException(alertRule.getDeviceId());
        }
        if (alertRule.getGroupId() != null && !deviceRegistryPort.groupExists(alertRule.getGroupId())) {
            throw new DeviceGroupNotFoundException(alertRule.getGroupId());
        }

        alertRuleRepositoryPort.save(alertRule);
        return AlertRuleResult.from(alertRule);
    }
}
