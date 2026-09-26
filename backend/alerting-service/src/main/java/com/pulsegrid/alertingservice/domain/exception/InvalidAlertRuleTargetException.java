package com.pulsegrid.alertingservice.domain.exception;

public class InvalidAlertRuleTargetException extends DomainException {
    public InvalidAlertRuleTargetException(){
        super(ErrorCode.INVALID_ALERT_RULE_TARGET);
    }
}
