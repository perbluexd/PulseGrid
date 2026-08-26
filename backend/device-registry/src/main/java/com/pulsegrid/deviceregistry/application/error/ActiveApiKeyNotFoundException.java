package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class ActiveApiKeyNotFoundException extends ApplicationException {
    public ActiveApiKeyNotFoundException(){
        super(ErrorCode.ACTIVE_API_KEY_NOT_FOUND);
    }
}
