package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceAlreadyInGroupException extends ApplicationException {
    public DeviceAlreadyInGroupException(){
        super(ErrorCode.DEVICE_ALREADY_IN_GROUP);
    }
}
