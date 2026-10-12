package com.pulsegrid.aggregationservice.api.error;

import java.time.Instant;

public record ErrorResponse(String message, int status, Instant timestamp) {
}
