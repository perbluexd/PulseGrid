package com.pulsegrid.ingestiongateway.application.error;

import com.pulsegrid.ingestiongateway.domain.exception.ErrorCode;

public abstract class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;

    protected ApplicationException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
