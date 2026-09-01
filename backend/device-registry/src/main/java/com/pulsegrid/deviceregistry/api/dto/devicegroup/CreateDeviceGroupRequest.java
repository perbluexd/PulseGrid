package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceGroupRequest(
        @NotBlank(message = "El nombre del grupo es obligatorio")
        String name,

        String description
) {
}
