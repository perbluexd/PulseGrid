package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class DashboardUserInactiveException extends ApplicationException {
    public DashboardUserInactiveException(String userId){
        super(ErrorCode.DASHBOARD_USER_INACTIVE, "La cuenta de dashboard " + userId + " se encuentra desactivada");
    }
}
