package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

public class DeviceRegistryUnavailableException extends ApplicationException {
    public DeviceRegistryUnavailableException(Throwable cause){
        super(ErrorCode.DEVICE_REGISTRY_UNAVAILABLE, ErrorCode.DEVICE_REGISTRY_UNAVAILABLE.getDefaultMessage(), cause);
    }
}
