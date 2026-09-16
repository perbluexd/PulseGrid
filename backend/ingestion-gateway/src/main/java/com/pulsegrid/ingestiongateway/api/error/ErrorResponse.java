package com.pulsegrid.ingestiongateway.api.error;

import java.time.Instant;

public record ErrorResponse(String code, String message, int status, Instant timestamp) {
}
