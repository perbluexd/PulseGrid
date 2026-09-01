package com.pulsegrid.deviceregistry.api.dto.device;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
        @NotBlank(message = "El nombre del dispositivo es obligatorio")
        String name,

        @NotBlank(message = "El tipo de dispositivo es obligatorio")
        String type,

        @NotBlank(message = "La ubicación del dispositivo es obligatoria")
        String location
) {
}
