package com.pulsegrid.aggregationservice.application.error;

public class InvalidTimeRangeException extends RuntimeException {
    public InvalidTimeRangeException(String message) {
        super(message);
    }
}
