package com.pulsegrid.alertingservice.domain.exception;

import com.pulsegrid.alertingservice.domain.model.AlertStatus;

public class InvalidAlertStatusTransitionException extends DomainException {
    public InvalidAlertStatusTransitionException(AlertStatus from, AlertStatus to){
        super(ErrorCode.INVALID_ALERT_STATUS_TRANSITION, "La alerta no puede pasar de " + from + " a " + to);
    }
}
