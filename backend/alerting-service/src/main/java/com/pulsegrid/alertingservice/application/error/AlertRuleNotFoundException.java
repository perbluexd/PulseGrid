package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

import java.util.UUID;

public class AlertRuleNotFoundException extends ApplicationException {
    public AlertRuleNotFoundException(UUID alertRuleId){
        super(ErrorCode.ALERT_RULE_NOT_FOUND, "La regla de alerta " + alertRuleId + " no fue encontrada");
    }
}
