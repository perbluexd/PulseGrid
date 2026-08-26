package com.pulsegrid.deviceregistry.application.error;

import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;

public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException(){
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
