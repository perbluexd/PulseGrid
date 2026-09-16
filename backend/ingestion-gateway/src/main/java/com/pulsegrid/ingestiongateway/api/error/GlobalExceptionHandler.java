package com.pulsegrid.ingestiongateway.api.error;

import com.pulsegrid.ingestiongateway.application.error.ApplicationException;
import com.pulsegrid.ingestiongateway.domain.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(WebExchangeBindException ex) {
        String message = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorResponse body = new ErrorResponse("ERR-000", message, HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(ServerWebInputException ex) {
        ErrorResponse body = new ErrorResponse("ERR-000", ex.getReason(), HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        HttpStatus status = mapToStatus(ex.getErrorCode());
        ErrorResponse body = new ErrorResponse(ex.getErrorCode().getCode(), ex.getMessage(), status.value(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Error inesperado", ex);
        ErrorResponse body = new ErrorResponse("ERR-500", "Ocurrió un error interno inesperado",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus mapToStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_API_KEY -> HttpStatus.UNAUTHORIZED;
            case DEVICE_NOT_ACTIVE -> HttpStatus.FORBIDDEN;
        };
    }
}
