package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.internal.DeviceGroupsResponse;
import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyRequest;
import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyResponse;
import com.pulsegrid.deviceregistry.api.mapper.DeviceGroupsResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ResolveByApiKeyResponseMapper;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ResolveDeviceByApiKeyUseCase;
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
public class InternalDeviceController {

    private final ResolveDeviceByApiKeyUseCase resolveDeviceByApiKeyUseCase;
    private final ResolveByApiKeyResponseMapper resolveByApiKeyResponseMapper;
    private final GetDeviceGroupsUseCase getDeviceGroupsUseCase;
    private final DeviceGroupsResponseMapper deviceGroupsResponseMapper;

    @PostMapping("/resolve-by-key")
    public ResponseEntity<ResolveByApiKeyResponse> resolveByApiKey(@Valid @RequestBody ResolveByApiKeyRequest request) {
        var command = new ResolveDeviceByApiKeyCommand(request.apiKey());
        var result = resolveDeviceByApiKeyUseCase.resolve(command);
        var response = resolveByApiKeyResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<DeviceGroupsResponse> getGroups(@PathVariable UUID id) {
        var command = new GetDeviceGroupsCommand(id);
        var result = getDeviceGroupsUseCase.getGroups(command);
        var response = deviceGroupsResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
