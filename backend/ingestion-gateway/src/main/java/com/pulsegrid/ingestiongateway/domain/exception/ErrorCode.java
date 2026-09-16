package com.pulsegrid.ingestiongateway.domain.exception;

public enum ErrorCode {
    INVALID_API_KEY("ERR-004", "La API key es inválida o fue revocada"),
    DEVICE_NOT_ACTIVE("ERR-005", "El dispositivo no se encuentra activo");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
