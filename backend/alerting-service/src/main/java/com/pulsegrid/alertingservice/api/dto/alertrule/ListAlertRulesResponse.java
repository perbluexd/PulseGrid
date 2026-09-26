package com.pulsegrid.alertingservice.api.dto.alertrule;

import java.util.List;

public record ListAlertRulesResponse(List<AlertRuleResponse> alertRules) {
}
