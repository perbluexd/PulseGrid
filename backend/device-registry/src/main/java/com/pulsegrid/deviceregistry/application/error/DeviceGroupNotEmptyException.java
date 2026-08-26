package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceGroupNotEmptyException extends ApplicationException {
    public DeviceGroupNotEmptyException(){
        super(ErrorCode.DEVICE_GROUP_NOT_EMPTY);
    }
}
