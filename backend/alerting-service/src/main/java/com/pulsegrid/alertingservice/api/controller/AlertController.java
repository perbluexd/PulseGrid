package com.pulsegrid.alertingservice.api.controller;

import com.pulsegrid.alertingservice.api.dto.alert.AlertResponse;
import com.pulsegrid.alertingservice.api.dto.alert.ListAlertsResponse;
import com.pulsegrid.alertingservice.api.error.ErrorResponse;
import com.pulsegrid.alertingservice.api.mapper.AlertResponseMapper;
import com.pulsegrid.alertingservice.application.command.AcknowledgeAlertCommand;
import com.pulsegrid.alertingservice.application.command.GetAlertCommand;
import com.pulsegrid.alertingservice.application.command.ListAlertsCommand;
import com.pulsegrid.alertingservice.application.command.ResolveAlertCommand;
import com.pulsegrid.alertingservice.application.port.in.AcknowledgeAlertUseCase;
import com.pulsegrid.alertingservice.application.port.in.GetAlertUseCase;
import com.pulsegrid.alertingservice.application.port.in.ListAlertsUseCase;
import com.pulsegrid.alertingservice.application.port.in.ResolveAlertUseCase;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alertas disparadas y su ciclo de vida (ADMIN reconoce/resuelve, ADMIN/VIEWER leen)")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final ListAlertsUseCase listAlertsUseCase;
    private final GetAlertUseCase getAlertUseCase;
    private final AcknowledgeAlertUseCase acknowledgeAlertUseCase;
    private final ResolveAlertUseCase resolveAlertUseCase;
    private final AlertResponseMapper alertResponseMapper;

    @Operation(summary = "Listar alertas", description = "Filtros opcionales combinables, más recientes primero")
    @ApiResponse(responseCode = "200", description = "Listado de alertas")
    @GetMapping
    public ResponseEntity<ListAlertsResponse> list(@RequestParam(required = false) AlertStatus status,
                                                   @RequestParam(required = false) AlertSeverity severity,
                                                   @RequestParam(required = false) UUID deviceId) {
        var results = listAlertsUseCase.list(new ListAlertsCommand(status, severity, deviceId));
        return ResponseEntity.ok(alertResponseMapper.toResponse(results));
    }

    @Operation(summary = "Obtener una alerta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta encontrada"),
            @ApiResponse(responseCode = "404", description = "La alerta no existe (ERR-003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> get(@Parameter(description = "Id de la alerta") @PathVariable UUID id) {
        return ResponseEntity.ok(alertResponseMapper.toResponse(getAlertUseCase.get(new GetAlertCommand(id))));
    }

    @Operation(summary = "Reconocer una alerta", description = "Solo desde TRIGGERED; acknowledgedBy sale del JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta reconocida"),
            @ApiResponse(responseCode = "404", description = "La alerta no existe (ERR-003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transición de estado inválida (ERR-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledge(@Parameter(description = "Id de la alerta") @PathVariable UUID id,
                                                     @AuthenticationPrincipal String userId) {
        var result = acknowledgeAlertUseCase.acknowledge(new AcknowledgeAlertCommand(id, UUID.fromString(userId)));
        return ResponseEntity.ok(alertResponseMapper.toResponse(result));
    }

    @Operation(summary = "Resolver una alerta", description = "Desde TRIGGERED o ACKNOWLEDGED; resolvedBy sale del JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta resuelta"),
            @ApiResponse(responseCode = "404", description = "La alerta no existe (ERR-003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La alerta ya estaba resuelta (ERR-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolve(@Parameter(description = "Id de la alerta") @PathVariable UUID id,
                                                 @AuthenticationPrincipal String userId) {
        var result = resolveAlertUseCase.resolve(new ResolveAlertCommand(id, UUID.fromString(userId)));
        return ResponseEntity.ok(alertResponseMapper.toResponse(result));
    }
}
