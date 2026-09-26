package com.pulsegrid.alertingservice.api.mapper;

import com.pulsegrid.alertingservice.api.dto.alertrule.AlertRuleResponse;
import com.pulsegrid.alertingservice.api.dto.alertrule.ListAlertRulesResponse;
import com.pulsegrid.alertingservice.application.port.result.AlertRuleResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertRuleResponseMapper {

    public AlertRuleResponse toResponse(AlertRuleResult result) {
        return new AlertRuleResponse(result.alertRuleId(), result.deviceId(), result.groupId(), result.metricType(),
                result.condition(), result.threshold(), result.severity(), result.active(), result.createdAt(),
                result.updatedAt());
    }

    public ListAlertRulesResponse toResponse(List<AlertRuleResult> results) {
        return new ListAlertRulesResponse(results.stream().map(this::toResponse).toList());
    }
}
