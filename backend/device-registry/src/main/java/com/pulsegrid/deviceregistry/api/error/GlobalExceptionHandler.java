package com.pulsegrid.deviceregistry.api.error;

import com.pulsegrid.deviceregistry.application.error.ApplicationException;
import com.pulsegrid.deviceregistry.domain.exception.DomainException;
import com.pulsegrid.deviceregistry.domain.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex){
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
            case INVALID_EMAIL_FORMAT -> HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS, INVALID_API_KEY -> HttpStatus.UNAUTHORIZED;
            case DASHBOARD_USER_INACTIVE, DEVICE_NOT_ACTIVE -> HttpStatus.FORBIDDEN;
            case DEVICE_NOT_FOUND, DEVICE_GROUP_NOT_FOUND, ACTIVE_API_KEY_NOT_FOUND, DEVICE_NOT_IN_GROUP ->
                    HttpStatus.NOT_FOUND;
            case DEVICE_ALREADY_IN_GROUP, DEVICE_GROUP_NOT_EMPTY -> HttpStatus.CONFLICT;
        };
    }
}
