package com.pulsegrid.ingestiongateway.application.error;

import com.pulsegrid.ingestiongateway.domain.exception.ErrorCode;

public class DeviceNotActiveException extends ApplicationException {
    public DeviceNotActiveException() {
        super(ErrorCode.DEVICE_NOT_ACTIVE);
    }
}
