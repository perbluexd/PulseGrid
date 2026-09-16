package com.pulsegrid.ingestiongateway.application.error;

import com.pulsegrid.ingestiongateway.domain.exception.ErrorCode;

public class InvalidApiKeyException extends ApplicationException {
    public InvalidApiKeyException() {
        super(ErrorCode.INVALID_API_KEY);
    }
}
