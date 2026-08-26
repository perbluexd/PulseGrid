package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceNotInGroupException extends ApplicationException {
    public DeviceNotInGroupException(){
        super(ErrorCode.DEVICE_NOT_IN_GROUP);
    }
}
