package com.pulsegrid.deviceregistry.domain.exception;

public enum ErrorCode {
    INVALID_EMAIL_FORMAT("ERR-001", "El formato del email es invalido"),
    INVALID_CREDENTIALS("ERR-002", "Credenciales inválidas"),
    DASHBOARD_USER_INACTIVE("ERR-003", "La cuenta de dashboard se encuentra desactivada"),
    INVALID_API_KEY("ERR-004", "La API key es inválida o fue revocada"),
    DEVICE_NOT_ACTIVE("ERR-005", "El dispositivo no se encuentra activo"),
    DEVICE_NOT_FOUND("ERR-006", "El dispositivo no fue encontrado"),
    DEVICE_GROUP_NOT_FOUND("ERR-007", "El grupo de dispositivos no fue encontrado"),
    DEVICE_ALREADY_IN_GROUP("ERR-008", "El dispositivo ya pertenece a este grupo"),
    ACTIVE_API_KEY_NOT_FOUND("ERR-009", "El dispositivo no tiene una API key activa para rotar"),
    DEVICE_NOT_IN_GROUP("ERR-010", "El dispositivo no pertenece a este grupo"),
    DEVICE_GROUP_NOT_EMPTY("ERR-011", "El grupo todavía tiene dispositivos asociados, no se puede borrar"),
    INVALID_OR_EXPIRED_TOKEN("ERR-012", "El token de acceso es inválido o expiró"),
    INSUFFICIENT_PERMISSIONS("ERR-013", "No tenés permisos para realizar esta acción");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage){
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    public String getCode(){
        return code;
    }
    public String getDefaultMessage(){
        return defaultMessage;
    }
}
