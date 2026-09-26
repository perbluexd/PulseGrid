package com.pulsegrid.alertingservice.api.dto.alertrule;

import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAlertRuleRequest(
        UUID deviceId,

        UUID groupId,

        @NotBlank(message = "El tipo de métrica es obligatorio")
        @Size(max = 50, message = "El tipo de métrica no puede superar los 50 caracteres")
        String metricType,

        @NotNull(message = "La condición es obligatoria")
        AlertCondition condition,

        @NotNull(message = "El umbral es obligatorio")
        Double threshold,

        @NotNull(message = "La severidad es obligatoria")
        AlertSeverity severity
) {
}
