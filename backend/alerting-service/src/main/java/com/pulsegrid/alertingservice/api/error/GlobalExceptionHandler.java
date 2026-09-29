package com.pulsegrid.alertingservice.api.error;

import com.pulsegrid.alertingservice.application.error.ApplicationException;
import com.pulsegrid.alertingservice.domain.exception.DomainException;
import com.pulsegrid.alertingservice.domain.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorResponse body = new ErrorResponse("ERR-000", message, HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException ex){
        ErrorResponse body = new ErrorResponse("ERR-000", "El cuerpo de la petición es inválido o tiene valores no permitidos",
                HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex){
        ErrorResponse body = new ErrorResponse("ERR-000", "El parámetro '" + ex.getName() + "' tiene un valor inválido",
                HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex){
        if (ex.getCause() != null) {
            log.warn("{}: {}", ex.getErrorCode().getCode(), ex.getMessage(), ex.getCause());
        }
        return buildResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex){
        return buildResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex){
        log.error("Error inesperado", ex);
        ErrorResponse body = new ErrorResponse("ERR-500", "Ocurrió un error interno inesperado",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String message){
        HttpStatus status = mapToStatus(errorCode);
        ErrorResponse body = new ErrorResponse(errorCode.getCode(), message, status.value(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus mapToStatus(ErrorCode errorCode){
        return switch (errorCode) {
            case INVALID_ALERT_RULE_TARGET -> HttpStatus.BAD_REQUEST;
            case INVALID_OR_EXPIRED_TOKEN -> HttpStatus.UNAUTHORIZED;
            case INSUFFICIENT_PERMISSIONS -> HttpStatus.FORBIDDEN;
            case ALERT_RULE_NOT_FOUND, ALERT_NOT_FOUND, DEVICE_NOT_FOUND, DEVICE_GROUP_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_ALERT_STATUS_TRANSITION -> HttpStatus.CONFLICT;
            case DEVICE_REGISTRY_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}
