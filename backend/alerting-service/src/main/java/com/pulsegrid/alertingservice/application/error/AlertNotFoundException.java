package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

import java.util.UUID;

public class AlertNotFoundException extends ApplicationException {
    public AlertNotFoundException(UUID alertId){
        super(ErrorCode.ALERT_NOT_FOUND, "La alerta " + alertId + " no fue encontrada");
    }
}
