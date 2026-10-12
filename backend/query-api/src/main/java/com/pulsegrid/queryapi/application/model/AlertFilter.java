package com.pulsegrid.queryapi.application.model;

import java.util.UUID;

public record AlertFilter(AlertStatus status, AlertSeverity severity, UUID deviceId) {

    public static AlertFilter none() {
        return new AlertFilter(null, null, null);
    }
}
