package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DeviceGroupNotFoundException extends ApplicationException {
    public DeviceGroupNotFoundException(String groupId){
        super(ErrorCode.DEVICE_GROUP_NOT_FOUND, "El grupo de dispositivos " + groupId + " no fue encontrado");
    }
}
