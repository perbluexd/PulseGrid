package com.pulsegrid.notificationworkers.application.error;

public class NotificationDeliveryUnavailableException extends RuntimeException {

    public NotificationDeliveryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
