package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceNotActiveException extends ApplicationException {
    public DeviceNotActiveException(String deviceId){
        super(ErrorCode.DEVICE_NOT_ACTIVE, "El dispositivo " + deviceId + " no se encuentra activo");
    }
}
