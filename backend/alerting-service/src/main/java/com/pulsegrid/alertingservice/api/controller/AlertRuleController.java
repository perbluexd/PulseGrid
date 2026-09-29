package com.pulsegrid.alertingservice.api.controller;

import com.pulsegrid.alertingservice.api.dto.alertrule.AlertRuleResponse;
import com.pulsegrid.alertingservice.api.dto.alertrule.ChangeAlertRuleStatusRequest;
import com.pulsegrid.alertingservice.api.dto.alertrule.CreateAlertRuleRequest;
import com.pulsegrid.alertingservice.api.dto.alertrule.ListAlertRulesResponse;
import com.pulsegrid.alertingservice.api.dto.alertrule.UpdateAlertRuleRequest;
import com.pulsegrid.alertingservice.api.error.ErrorResponse;
import com.pulsegrid.alertingservice.api.mapper.AlertRuleRequestMapper;
import com.pulsegrid.alertingservice.api.mapper.AlertRuleResponseMapper;
import com.pulsegrid.alertingservice.application.command.ChangeAlertRuleStatusCommand;
import com.pulsegrid.alertingservice.application.command.GetAlertRuleCommand;
import com.pulsegrid.alertingservice.application.port.in.ChangeAlertRuleStatusUseCase;
import com.pulsegrid.alertingservice.application.port.in.CreateAlertRuleUseCase;
import com.pulsegrid.alertingservice.application.port.in.GetAlertRuleUseCase;
import com.pulsegrid.alertingservice.application.port.in.ListAlertRulesUseCase;
import com.pulsegrid.alertingservice.application.port.in.UpdateAlertRuleUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alert-rules")
@RequiredArgsConstructor
@Tag(name = "Alert Rules", description = "Reglas de alerta por dispositivo o por grupo (ADMIN escribe, ADMIN/VIEWER leen)")
@SecurityRequirement(name = "bearerAuth")
public class AlertRuleController {

    private final CreateAlertRuleUseCase createAlertRuleUseCase;
    private final UpdateAlertRuleUseCase updateAlertRuleUseCase;
    private final ChangeAlertRuleStatusUseCase changeAlertRuleStatusUseCase;
    private final GetAlertRuleUseCase getAlertRuleUseCase;
    private final ListAlertRulesUseCase listAlertRulesUseCase;
    private final AlertRuleRequestMapper alertRuleRequestMapper;
    private final AlertRuleResponseMapper alertRuleResponseMapper;

    @Operation(summary = "Crear una regla de alerta", description = "Debe apuntar a un deviceId o a un groupId, nunca a ambos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Regla creada"),
            @ApiResponse(responseCode = "400", description = "Body inválido (ERR-000) o target inválido (ERR-001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El dispositivo (ERR-005) o el grupo (ERR-006) no existen",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Device Registry no disponible (ERR-007)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AlertRuleResponse> create(@Valid @RequestBody CreateAlertRuleRequest request) {
        var result = createAlertRuleUseCase.create(alertRuleRequestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(alertRuleResponseMapper.toResponse(result));
    }

    @Operation(summary = "Editar la definición de una regla", description = "El target (deviceId/groupId) no se puede cambiar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regla actualizada"),
            @ApiResponse(responseCode = "400", description = "Body inválido (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La regla no existe (ERR-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AlertRuleResponse> update(@Parameter(description = "Id de la regla") @PathVariable UUID id,
                                                    @Valid @RequestBody UpdateAlertRuleRequest request) {
        var result = updateAlertRuleUseCase.update(alertRuleRequestMapper.toCommand(id, request));
        return ResponseEntity.ok(alertRuleResponseMapper.toResponse(result));
    }

    @Operation(summary = "Activar o desactivar una regla")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "La regla no existe (ERR-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AlertRuleResponse> changeStatus(@Parameter(description = "Id de la regla") @PathVariable UUID id,
                                                          @Valid @RequestBody ChangeAlertRuleStatusRequest request) {
        var result = changeAlertRuleStatusUseCase.changeStatus(new ChangeAlertRuleStatusCommand(id, request.active()));
        return ResponseEntity.ok(alertRuleResponseMapper.toResponse(result));
    }

    @Operation(summary = "Obtener una regla por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regla encontrada"),
            @ApiResponse(responseCode = "404", description = "La regla no existe (ERR-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlertRuleResponse> get(@Parameter(description = "Id de la regla") @PathVariable UUID id) {
        var result = getAlertRuleUseCase.get(new GetAlertRuleCommand(id));
        return ResponseEntity.ok(alertRuleResponseMapper.toResponse(result));
    }

    @Operation(summary = "Listar todas las reglas")
    @ApiResponse(responseCode = "200", description = "Listado de reglas, más recientes primero")
    @GetMapping
    public ResponseEntity<ListAlertRulesResponse> list() {
        return ResponseEntity.ok(alertRuleResponseMapper.toResponse(listAlertRulesUseCase.listAll()));
    }
}
