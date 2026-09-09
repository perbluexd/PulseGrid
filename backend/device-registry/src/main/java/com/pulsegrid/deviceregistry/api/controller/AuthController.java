package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.auth.LoginRequest;
import com.pulsegrid.deviceregistry.api.dto.auth.LoginResponse;
import com.pulsegrid.deviceregistry.api.error.ErrorResponse;
import com.pulsegrid.deviceregistry.api.mapper.LoginResponseMapper;
import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.port.in.LoginDashboardUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login del dashboard, emite el JWT usado por el resto de la API")
public class AuthController {

    private final LoginDashboardUserUseCase loginDashboardUserUseCase;
    private final LoginResponseMapper loginResponseMapper;

    @Operation(summary = "Iniciar sesión", description = "Valida email y contraseña de un DashboardUser y devuelve un JWT de acceso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el JWT"),
            @ApiResponse(responseCode = "400", description = "Email o contraseña vacíos (ERR-000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (ERR-002)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "La cuenta de dashboard está inactiva (ERR-003)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = new LoginDashboardUserCommand(request.email(), request.password());
        var result = loginDashboardUserUseCase.login(command);
        var response = loginResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
