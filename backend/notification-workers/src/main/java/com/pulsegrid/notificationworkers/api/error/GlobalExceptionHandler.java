package com.pulsegrid.notificationworkers.api.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadParameters(Exception ex) {
        var body = new ErrorResponse("Parámetros inválidos: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
