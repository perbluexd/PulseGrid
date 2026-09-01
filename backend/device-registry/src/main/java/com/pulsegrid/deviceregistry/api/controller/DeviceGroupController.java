package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.api.dto.devicegroup.AddDeviceToGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.GetDeviceGroupResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.ListDeviceGroupsResponse;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.UpdateDeviceGroupRequest;
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

    @PostMapping
    public ResponseEntity<CreateDeviceGroupResponse> create(@Valid @RequestBody CreateDeviceGroupRequest request) {
        var command = createDeviceGroupRequestMapper.toCommand(request);
        var result = createDeviceGroupUseCase.create(command);
        var response = createDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetDeviceGroupResponse> get(@PathVariable UUID id) {
        var command = new GetDeviceGroupCommand(id);
        var result = getDeviceGroupUseCase.get(command);
        var response = getDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<ListDeviceGroupsResponse> list() {
        var result = listDeviceGroupsUseCase.listAll();
        var response = listDeviceGroupsResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetDeviceGroupResponse> update(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateDeviceGroupRequest request) {
        var command = updateDeviceGroupRequestMapper.toCommand(id, request);
        var result = updateDeviceGroupUseCase.update(command);
        var response = getDeviceGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        var command = new DeleteDeviceGroupCommand(id);
        deleteDeviceGroupUseCase.delete(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<AddDeviceToGroupResponse> addDevice(@PathVariable UUID groupId, @PathVariable UUID deviceId) {
        var command = new AddDeviceToGroupCommand(deviceId, groupId);
        var result = addDeviceToGroupUseCase.addToGroup(command);
        var response = addDeviceToGroupResponseMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<Void> removeDevice(@PathVariable UUID groupId, @PathVariable UUID deviceId) {
        var command = new RemoveDeviceFromGroupCommand(deviceId, groupId);
        removeDeviceFromGroupUseCase.remove(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
