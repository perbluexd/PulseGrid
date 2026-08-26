package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class InvalidApiKeyException extends ApplicationException {
    public InvalidApiKeyException(){
        super(ErrorCode.INVALID_API_KEY);
    }
}
