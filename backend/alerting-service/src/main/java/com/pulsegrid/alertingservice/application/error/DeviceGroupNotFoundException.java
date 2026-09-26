package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

import java.util.UUID;

public class DeviceGroupNotFoundException extends ApplicationException {
    public DeviceGroupNotFoundException(UUID groupId){
        super(ErrorCode.DEVICE_GROUP_NOT_FOUND, "El grupo " + groupId + " no existe en Device Registry");
    }
}
