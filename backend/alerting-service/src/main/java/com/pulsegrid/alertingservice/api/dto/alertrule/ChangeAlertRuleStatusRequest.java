package com.pulsegrid.alertingservice.api.dto.alertrule;

import jakarta.validation.constraints.NotNull;

public record ChangeAlertRuleStatusRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean active
) {
}
