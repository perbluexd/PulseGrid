package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertRuleView;
import com.pulsegrid.queryapi.application.model.AlertSeverity;
import com.pulsegrid.queryapi.application.model.AlertStatus;
import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.port.out.AlertingPort;
import com.pulsegrid.queryapi.infrastructure.client.dto.AlertListResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.AlertResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.AlertRuleListResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.AlertRuleResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class AlertingRestClientAdapter implements AlertingPort {

    private static final String SERVICE = "alerting-service";

    private final RestClient restClient;

    public AlertingRestClientAdapter(@Qualifier("alertingRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<AlertView> findAlerts(AlertFilter filter) {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri(uri -> uri.path("/api/v1/alerts")
                        .queryParamIfPresent("status", Optional.ofNullable(filter.status()))
                        .queryParamIfPresent("severity", Optional.ofNullable(filter.severity()))
                        .queryParamIfPresent("deviceId", Optional.ofNullable(filter.deviceId()))
                        .build())
                .retrieve()
                .body(AlertListResponse.class));
        return response.alerts().stream().map(this::toView).toList();
    }

    @Override
    public List<AlertRuleView> findAllRules() {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri("/api/v1/alert-rules")
                .retrieve()
                .body(AlertRuleListResponse.class));
        return response.alertRules().stream().map(this::toView).toList();
    }

    private AlertView toView(AlertResponse alert) {
        return new AlertView(alert.alertId(), alert.alertRuleId(), alert.deviceId(), alert.triggeredValue(),
                AlertSeverity.valueOf(alert.severity()), AlertStatus.valueOf(alert.status()),
                alert.triggeredAt(), alert.resolvedAt());
    }

    private AlertRuleView toView(AlertRuleResponse rule) {
        return new AlertRuleView(rule.alertRuleId(), rule.deviceId(), rule.groupId(), rule.metricType(),
                rule.condition(), rule.threshold(), AlertSeverity.valueOf(rule.severity()), rule.active(),
                rule.createdAt());
    }
}
