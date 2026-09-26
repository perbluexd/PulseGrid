package com.pulsegrid.alertingservice.application.error;

import com.pulsegrid.alertingservice.domain.exception.ErrorCode;

public abstract class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;

    protected ApplicationException(ErrorCode errorCode){
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
    protected ApplicationException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }
    protected ApplicationException(ErrorCode errorCode, String message, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode(){
        return errorCode;
    }
}
