package com.pulsegrid.alertingservice.api.mapper;

import com.pulsegrid.alertingservice.api.dto.alertrule.CreateAlertRuleRequest;
import com.pulsegrid.alertingservice.api.dto.alertrule.UpdateAlertRuleRequest;
import com.pulsegrid.alertingservice.application.command.CreateAlertRuleCommand;
import com.pulsegrid.alertingservice.application.command.UpdateAlertRuleCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AlertRuleRequestMapper {

    public CreateAlertRuleCommand toCommand(CreateAlertRuleRequest request) {
        return new CreateAlertRuleCommand(request.deviceId(), request.groupId(), request.metricType(),
                request.condition(), request.threshold(), request.severity());
    }

    public UpdateAlertRuleCommand toCommand(UUID alertRuleId, UpdateAlertRuleRequest request) {
        return new UpdateAlertRuleCommand(alertRuleId, request.metricType(), request.condition(),
                request.threshold(), request.severity());
    }
}
