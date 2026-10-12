package com.pulsegrid.notificationworkers.api.error;

import java.time.Instant;

public record ErrorResponse(String message, int status, Instant timestamp) {
}
