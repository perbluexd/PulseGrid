package com.pulsegrid.alertingservice.api.mapper;

import com.pulsegrid.alertingservice.api.dto.alert.AlertResponse;
import com.pulsegrid.alertingservice.api.dto.alert.ListAlertsResponse;
import com.pulsegrid.alertingservice.application.port.result.AlertResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertResponseMapper {

    public AlertResponse toResponse(AlertResult result) {
        return new AlertResponse(result.alertId(), result.alertRuleId(), result.deviceId(), result.triggeredValue(),
                result.severity(), result.status(), result.triggeredAt(), result.resolvedAt(),
                result.acknowledgedBy(), result.resolvedBy());
    }

    public ListAlertsResponse toResponse(List<AlertResult> results) {
        return new ListAlertsResponse(results.stream().map(this::toResponse).toList());
    }
}
