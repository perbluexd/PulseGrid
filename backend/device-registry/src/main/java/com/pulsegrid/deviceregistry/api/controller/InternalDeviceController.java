package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.internal.DeviceGroupsResponse;
import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyRequest;
import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyResponse;
import com.pulsegrid.deviceregistry.api.error.ErrorResponse;
import com.pulsegrid.deviceregistry.api.mapper.DeviceGroupsResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ResolveByApiKeyResponseMapper;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ResolveDeviceByApiKeyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/devices")
@RequiredArgsConstructor
@Tag(name = "Internal Devices", description = "Tráfico servicio-a-servicio, autenticado con X-Internal-Service, no con JWT")
@SecurityRequirement(name = "internalServiceAuth")
public class InternalDeviceController {

    private final ResolveDeviceByApiKeyUseCase resolveDeviceByApiKeyUseCase;
    private final ResolveByApiKeyResponseMapper resolveByApiKeyResponseMapper;
    private final GetDeviceGroupsUseCase getDeviceGroupsUseCase;
    private final DeviceGroupsResponseMapper deviceGroupsResponseMapper;

    @Operation(summary = "Resolver un dispositivo por su ApiKey", description = "Usado por Ingestion Gateway para identificar al dispositivo dueño de una ApiKey cruda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ApiKey válida, devuelve el dispositivo"),
            @ApiResponse(responseCode = "400", description = "La ApiKey viene vacía (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "La ApiKey es inválida o fue revocada (ERR-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El dispositivo no está activo (ERR-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resolve-by-key")
    public ResponseEntity<ResolveByApiKeyResponse> resolveByApiKey(@Valid @RequestBody ResolveByApiKeyRequest request) {
        var command = new ResolveDeviceByApiKeyCommand(request.apiKey());
        var result = resolveDeviceByApiKeyUseCase.resolve(command);
        var response = resolveByApiKeyResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Obtener los grupos de un dispositivo", description = "Usado por Alerting Service para poblar su cache Redis de deviceId → groupIds")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ids de los grupos a los que pertenece el dispositivo"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/groups")
    public ResponseEntity<DeviceGroupsResponse> getGroups(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new GetDeviceGroupsCommand(id);
        var result = getDeviceGroupsUseCase.getGroups(command);
        var response = deviceGroupsResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
