package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.device.GetDeviceResponse;
import com.pulsegrid.deviceregistry.api.dto.device.ListDevicesResponse;
import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceRequest;
import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceResponse;
import com.pulsegrid.deviceregistry.api.dto.device.RotateApiKeyResponse;
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

    @PostMapping
    public ResponseEntity<RegisterDeviceResponse> register(@Valid @RequestBody RegisterDeviceRequest request) {
        var command = registerDeviceRequestMapper.toCommand(request);
        var result = registerDeviceUseCase.register(command);
        var response = registerDeviceResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetDeviceResponse> get(@PathVariable UUID id) {
        var command = new GetDeviceCommand(id);
        var result = getDeviceUseCase.get(command);
        var response = getDeviceResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<ListDevicesResponse> list() {
        var result = listDevicesUseCase.listAll();
        var response = listDevicesResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        var command = new DeactivateDeviceCommand(id);
        deactivateDeviceUseCase.deactivate(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable UUID id) {
        var command = new ReactivateDeviceCommand(id);
        reactivateDeviceUseCase.reactivate(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/decommission")
    public ResponseEntity<Void> decommission(@PathVariable UUID id) {
        var command = new DecommissionDeviceCommand(id);
        decommissionDeviceUseCase.decommission(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/api-key/rotate")
    public ResponseEntity<RotateApiKeyResponse> rotateApiKey(@PathVariable UUID id) {
        var command = new RotateDeviceApiKeyCommand(id);
        var result = rotateDeviceApiKeyUseCase.rotate(command);
        var response = rotateApiKeyResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
