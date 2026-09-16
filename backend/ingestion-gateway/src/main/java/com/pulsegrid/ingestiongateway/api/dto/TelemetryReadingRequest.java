package com.pulsegrid.ingestiongateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TelemetryReadingRequest(
        @NotBlank(message = "El tipo de métrica es obligatorio")
        String metricType,

        @NotNull(message = "El valor de la medición es obligatorio")
        Double value,

        @NotNull(message = "La fecha de la medición es obligatoria")
        Instant recordedAt
) {
}
