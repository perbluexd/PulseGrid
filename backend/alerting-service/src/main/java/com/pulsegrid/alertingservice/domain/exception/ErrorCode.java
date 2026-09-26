package com.pulsegrid.alertingservice.domain.exception;

public enum ErrorCode {
    INVALID_ALERT_RULE_TARGET("ERR-001", "La regla debe apuntar a un dispositivo o a un grupo, no a ambos ni a ninguno"),
    ALERT_RULE_NOT_FOUND("ERR-002", "La regla de alerta no fue encontrada"),
    ALERT_NOT_FOUND("ERR-003", "La alerta no fue encontrada"),
    INVALID_ALERT_STATUS_TRANSITION("ERR-004", "La alerta no admite ese cambio de estado"),
    DEVICE_NOT_FOUND("ERR-005", "El dispositivo no existe en Device Registry"),
    DEVICE_GROUP_NOT_FOUND("ERR-006", "El grupo de dispositivos no existe en Device Registry"),
    DEVICE_REGISTRY_UNAVAILABLE("ERR-007", "Device Registry no está disponible en este momento"),
    INVALID_OR_EXPIRED_TOKEN("ERR-008", "El token de acceso es inválido o expiró"),
    INSUFFICIENT_PERMISSIONS("ERR-009", "No tenés permisos para realizar esta acción");

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
