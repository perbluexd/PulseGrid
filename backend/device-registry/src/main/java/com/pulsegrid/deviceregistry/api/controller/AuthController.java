package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.auth.LoginRequest;
import com.pulsegrid.deviceregistry.api.dto.auth.LoginResponse;
import com.pulsegrid.deviceregistry.api.mapper.LoginResponseMapper;
import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.port.in.LoginDashboardUserUseCase;
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
public class AuthController {

    private final LoginDashboardUserUseCase loginDashboardUserUseCase;
    private final LoginResponseMapper loginResponseMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = new LoginDashboardUserCommand(request.email(), request.password());
        var result = loginDashboardUserUseCase.login(command);
        var response = loginResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
