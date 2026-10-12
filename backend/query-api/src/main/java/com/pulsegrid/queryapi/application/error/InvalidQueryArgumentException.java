package com.pulsegrid.queryapi.application.error;

public class InvalidQueryArgumentException extends RuntimeException {

    public InvalidQueryArgumentException(String message) {
        super(message);
    }
}
