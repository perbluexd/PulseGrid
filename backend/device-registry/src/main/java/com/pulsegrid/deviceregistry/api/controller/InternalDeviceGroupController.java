package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.internal.InternalDeviceGroupResponse;
import com.pulsegrid.deviceregistry.api.error.ErrorResponse;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/device-groups")
@RequiredArgsConstructor
@Tag(name = "Internal Device Groups", description = "Tráfico servicio-a-servicio, autenticado con X-Internal-Service, no con JWT")
@SecurityRequirement(name = "internalServiceAuth")
public class InternalDeviceGroupController {

    private final GetDeviceGroupUseCase getDeviceGroupUseCase;

    @Operation(summary = "Obtener un grupo por id", description = "Usado por Alerting Service para validar el groupId de una regla")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo encontrado"),
            @ApiResponse(responseCode = "404", description = "El grupo no existe (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<InternalDeviceGroupResponse> get(@Parameter(description = "Id del grupo") @PathVariable UUID id) {
        var result = getDeviceGroupUseCase.get(new GetDeviceGroupCommand(id));
        var response = new InternalDeviceGroupResponse(result.id(), result.name());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
