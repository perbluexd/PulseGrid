package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.device.GetDeviceResponse;
import com.pulsegrid.deviceregistry.api.dto.device.ListDevicesResponse;
import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceRequest;
import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceResponse;
import com.pulsegrid.deviceregistry.api.dto.device.RotateApiKeyResponse;
import com.pulsegrid.deviceregistry.api.error.ErrorResponse;
import com.pulsegrid.deviceregistry.api.mapper.GetDeviceResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ListDevicesResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.RegisterDeviceRequestMapper;
import com.pulsegrid.deviceregistry.api.mapper.RegisterDeviceResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.RotateApiKeyResponseMapper;
import com.pulsegrid.deviceregistry.application.command.DeactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.GetDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.ReactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.RotateDeviceApiKeyCommand;
import com.pulsegrid.deviceregistry.application.port.in.DeactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.DecommissionDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ListDevicesUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ReactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RegisterDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RotateDeviceApiKeyUseCase;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "CRUD y ciclo de vida de dispositivos (ADMIN escribe, ADMIN/VIEWER leen)")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final RegisterDeviceUseCase registerDeviceUseCase;
    private final RegisterDeviceRequestMapper registerDeviceRequestMapper;
    private final RegisterDeviceResponseMapper registerDeviceResponseMapper;
    private final GetDeviceUseCase getDeviceUseCase;
    private final GetDeviceResponseMapper getDeviceResponseMapper;
    private final ListDevicesUseCase listDevicesUseCase;
    private final ListDevicesResponseMapper listDevicesResponseMapper;
    private final DeactivateDeviceUseCase deactivateDeviceUseCase;
    private final ReactivateDeviceUseCase reactivateDeviceUseCase;
    private final DecommissionDeviceUseCase decommissionDeviceUseCase;
    private final RotateDeviceApiKeyUseCase rotateDeviceApiKeyUseCase;
    private final RotateApiKeyResponseMapper rotateApiKeyResponseMapper;

    @Operation(summary = "Registrar un dispositivo nuevo", description = "Crea el dispositivo y su primera ApiKey activa; la key cruda solo se devuelve acá")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dispositivo registrado"),
            @ApiResponse(responseCode = "400", description = "Campo obligatorio vacío, o tipo de dispositivo inválido (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RegisterDeviceResponse> register(@Valid @RequestBody RegisterDeviceRequest request) {
        var command = registerDeviceRequestMapper.toCommand(request);
        var result = registerDeviceUseCase.register(command);
        var response = registerDeviceResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener un dispositivo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispositivo encontrado"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GetDeviceResponse> get(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new GetDeviceCommand(id);
        var result = getDeviceUseCase.get(command);
        var response = getDeviceResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Listar todos los dispositivos")
    @ApiResponse(responseCode = "200", description = "Listado completo de dispositivos")
    @GetMapping
    public ResponseEntity<ListDevicesResponse> list() {
        var result = listDevicesUseCase.listAll();
        var response = listDevicesResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Desactivar un dispositivo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dispositivo desactivado"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new DeactivateDeviceCommand(id);
        deactivateDeviceUseCase.deactivate(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Reactivar un dispositivo desactivado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dispositivo reactivado"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new ReactivateDeviceCommand(id);
        reactivateDeviceUseCase.reactivate(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Dar de baja un dispositivo de forma definitiva")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dispositivo decomisionado"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/decommission")
    public ResponseEntity<Void> decommission(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new DecommissionDeviceCommand(id);
        decommissionDeviceUseCase.decommission(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Rotar la ApiKey de un dispositivo", description = "Revoca la key activa y genera una nueva; la key cruda nueva solo se devuelve acá")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ApiKey rotada"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006), o no tiene una ApiKey activa para rotar (ERR-009)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/api-key/rotate")
    public ResponseEntity<RotateApiKeyResponse> rotateApiKey(@Parameter(description = "Id del dispositivo") @PathVariable UUID id) {
        var command = new RotateDeviceApiKeyCommand(id);
        var result = rotateDeviceApiKeyUseCase.rotate(command);
        var response = rotateApiKeyResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
