package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.AddDeviceToGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.GetDeviceGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.ListDeviceGroupsResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.UpdateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.api.error.ErrorResponse;
import com.pulsegrid.deviceregistry.api.mapper.AddDeviceToGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.CreateDeviceGroupRequestMapper;
import com.pulsegrid.deviceregistry.api.mapper.CreateDeviceGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.GetDeviceGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ListDeviceGroupsResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.UpdateDeviceGroupRequestMapper;
import com.pulsegrid.deviceregistry.application.command.AddDeviceToGroupCommand;
import com.pulsegrid.deviceregistry.application.command.DeleteDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;
import com.pulsegrid.deviceregistry.application.port.in.AddDeviceToGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.CreateDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.DeleteDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ListDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RemoveDeviceFromGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.UpdateDeviceGroupUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/device-groups")
@RequiredArgsConstructor
@Tag(name = "Device Groups", description = "CRUD de grupos de dispositivos y su membership (ADMIN escribe, ADMIN/VIEWER leen)")
@SecurityRequirement(name = "bearerAuth")
public class DeviceGroupController {

    private final CreateDeviceGroupUseCase createDeviceGroupUseCase;
    private final CreateDeviceGroupRequestMapper createDeviceGroupRequestMapper;
    private final CreateDeviceGroupResponseMapper createDeviceGroupResponseMapper;

    private final GetDeviceGroupUseCase getDeviceGroupUseCase;
    private final GetDeviceGroupResponseMapper getDeviceGroupResponseMapper;

    private final ListDeviceGroupsUseCase listDeviceGroupsUseCase;
    private final ListDeviceGroupsResponseMapper listDeviceGroupsResponseMapper;

    private final UpdateDeviceGroupUseCase updateDeviceGroupUseCase;
    private final UpdateDeviceGroupRequestMapper updateDeviceGroupRequestMapper;

    private final DeleteDeviceGroupUseCase deleteDeviceGroupUseCase;

    private final AddDeviceToGroupUseCase addDeviceToGroupUseCase;
    private final AddDeviceToGroupResponseMapper addDeviceToGroupResponseMapper;

    private final RemoveDeviceFromGroupUseCase removeDeviceFromGroupUseCase;

    @Operation(summary = "Crear un grupo de dispositivos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo creado"),
            @ApiResponse(responseCode = "400", description = "El nombre del grupo está vacío (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CreateDeviceGroupResponse> create(@Valid @RequestBody CreateDeviceGroupRequest request) {
        var command = createDeviceGroupRequestMapper.toCommand(request);
        var result = createDeviceGroupUseCase.create(command);
        var response = createDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener un grupo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo encontrado"),
            @ApiResponse(responseCode = "404", description = "El grupo no existe (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GetDeviceGroupResponse> get(@Parameter(description = "Id del grupo") @PathVariable UUID id) {
        var command = new GetDeviceGroupCommand(id);
        var result = getDeviceGroupUseCase.get(command);
        var response = getDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Listar todos los grupos de dispositivos")
    @ApiResponse(responseCode = "200", description = "Listado completo de grupos")
    @GetMapping
    public ResponseEntity<ListDeviceGroupsResponse> list() {
        var result = listDeviceGroupsUseCase.listAll();
        var response = listDeviceGroupsResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Actualizar nombre/descripción de un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo actualizado"),
            @ApiResponse(responseCode = "400", description = "El nombre del grupo está vacío (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El grupo no existe (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<GetDeviceGroupResponse> update(@Parameter(description = "Id del grupo") @PathVariable UUID id,
                                                          @Valid @RequestBody UpdateDeviceGroupRequest request) {
        var command = updateDeviceGroupRequestMapper.toCommand(id, request);
        var result = updateDeviceGroupUseCase.update(command);
        var response = getDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Borrar un grupo de dispositivos", description = "Falla si el grupo todavía tiene dispositivos asociados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grupo borrado"),
            @ApiResponse(responseCode = "404", description = "El grupo no existe (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El grupo todavía tiene dispositivos asociados (ERR-011)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Id del grupo") @PathVariable UUID id) {
        var command = new DeleteDeviceGroupCommand(id);
        deleteDeviceGroupUseCase.delete(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Agregar un dispositivo a un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dispositivo agregado al grupo"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no existe (ERR-006), o el grupo no existe (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El dispositivo ya pertenece a este grupo (ERR-008)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<AddDeviceToGroupResponse> addDevice(@Parameter(description = "Id del grupo") @PathVariable UUID groupId,
                                                                @Parameter(description = "Id del dispositivo") @PathVariable UUID deviceId) {
        var command = new AddDeviceToGroupCommand(deviceId, groupId);
        var result = addDeviceToGroupUseCase.addToGroup(command);
        var response = addDeviceToGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Quitar un dispositivo de un grupo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dispositivo quitado del grupo"),
            @ApiResponse(responseCode = "404", description = "El dispositivo no pertenece a este grupo (ERR-010)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<Void> removeDevice(@Parameter(description = "Id del grupo") @PathVariable UUID groupId,
                                              @Parameter(description = "Id del dispositivo") @PathVariable UUID deviceId) {
        var command = new RemoveDeviceFromGroupCommand(deviceId, groupId);
        removeDeviceFromGroupUseCase.remove(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
