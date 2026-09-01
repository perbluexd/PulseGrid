package com.pulsegrid.deviceregistry.api.dto.devicegroup;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceGroupRequest(
        @NotBlank(message = "El nombre del grupo es obligatorio")
        String name,

        String description
) {
}
