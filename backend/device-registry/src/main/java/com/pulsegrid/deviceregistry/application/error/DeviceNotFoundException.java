package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceNotFoundException extends ApplicationException {
    public DeviceNotFoundException(String deviceId){
        super(ErrorCode.DEVICE_NOT_FOUND, "El dispositivo " + deviceId + " no fue encontrado");
    }
}
