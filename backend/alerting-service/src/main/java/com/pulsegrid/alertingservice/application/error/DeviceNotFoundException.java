package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

import java.util.UUID;

public class DeviceNotFoundException extends ApplicationException {
    public DeviceNotFoundException(UUID deviceId){
        super(ErrorCode.DEVICE_NOT_FOUND, "El dispositivo " + deviceId + " no existe en Device Registry");
    }
}
