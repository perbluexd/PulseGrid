package com.pulsegrid.deviceregistry.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.CreateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.api.dto.devicegroup.UpdateDeviceGroupRequest;
import com.pulsegrid.deviceregistry.api.mapper.AddDeviceToGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.CreateDeviceGroupRequestMapper;
import com.pulsegrid.deviceregistry.api.mapper.CreateDeviceGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.GetDeviceGroupResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ListDeviceGroupsResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.UpdateDeviceGroupRequestMapper;
import com.pulsegrid.deviceregistry.application.command.AddDeviceToGroupCommand;
import com.pulsegrid.deviceregistry.application.command.CreateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.command.DeleteDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;
import com.pulsegrid.deviceregistry.application.command.UpdateDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceAlreadyInGroupException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotEmptyException;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotInGroupException;
import com.pulsegrid.deviceregistry.application.port.in.AddDeviceToGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.CreateDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.DeleteDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ListDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RemoveDeviceFromGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.in.UpdateDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.result.AddDeviceToGroupResult;
import com.pulsegrid.deviceregistry.application.port.result.CreateDeviceGroupResult;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.application.port.result.ListDeviceGroupsResult;
import com.pulsegrid.deviceregistry.infrastructure.security.InternalServiceAuthenticationFilter;
import com.pulsegrid.deviceregistry.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DeviceGroupController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, InternalServiceAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
@Import({CreateDeviceGroupRequestMapper.class, CreateDeviceGroupResponseMapper.class, GetDeviceGroupResponseMapper.class,
        ListDeviceGroupsResponseMapper.class, UpdateDeviceGroupRequestMapper.class, AddDeviceToGroupResponseMapper.class})
class DeviceGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateDeviceGroupUseCase createDeviceGroupUseCase;

    @MockitoBean
    private GetDeviceGroupUseCase getDeviceGroupUseCase;

    @MockitoBean
    private ListDeviceGroupsUseCase listDeviceGroupsUseCase;

    @MockitoBean
    private UpdateDeviceGroupUseCase updateDeviceGroupUseCase;

    @MockitoBean
    private DeleteDeviceGroupUseCase deleteDeviceGroupUseCase;

    @MockitoBean
    private AddDeviceToGroupUseCase addDeviceToGroupUseCase;

    @MockitoBean
    private RemoveDeviceFromGroupUseCase removeDeviceFromGroupUseCase;

    @Test
    void shouldReturn201WhenGroupIsCreatedSuccessfully() throws Exception {
        CreateDeviceGroupRequest request = new CreateDeviceGroupRequest("Sensores Planta 1", "Grupo de sensores");
        CreateDeviceGroupCommand expectedCommand = new CreateDeviceGroupCommand("Sensores Planta 1", "Grupo de sensores");
        CreateDeviceGroupResult result = new CreateDeviceGroupResult(UUID.randomUUID(), "Sensores Planta 1", "Grupo de sensores");

        when(createDeviceGroupUseCase.create(expectedCommand)).thenReturn(result);

        mockMvc.perform(post("/api/v1/device-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sensores Planta 1"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateDeviceGroupRequest request = new CreateDeviceGroupRequest("", "Grupo de sensores");

        mockMvc.perform(post("/api/v1/device-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn200WithGroupWhenItExists() throws Exception {
        UUID groupId = UUID.randomUUID();
        GetDeviceGroupResult result = new GetDeviceGroupResult(groupId, "Sensores Planta 1", "Grupo de sensores");

        when(getDeviceGroupUseCase.get(new GetDeviceGroupCommand(groupId))).thenReturn(result);

        mockMvc.perform(get("/api/v1/device-groups/{id}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sensores Planta 1"));
    }

    @Test
    void shouldReturn404WhenGroupDoesNotExist() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(getDeviceGroupUseCase.get(new GetDeviceGroupCommand(groupId)))
                .thenThrow(new DeviceGroupNotFoundException(groupId.toString()));

        mockMvc.perform(get("/api/v1/device-groups/{id}", groupId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-007"));
    }

    @Test
    void shouldReturn200WithAllGroups() throws Exception {
        GetDeviceGroupResult group = new GetDeviceGroupResult(UUID.randomUUID(), "Sensores Planta 1", "Grupo de sensores");
        ListDeviceGroupsResult result = new ListDeviceGroupsResult(List.of(group));

        when(listDeviceGroupsUseCase.listAll()).thenReturn(result);

        mockMvc.perform(get("/api/v1/device-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.groups[0].name").value("Sensores Planta 1"));
    }

    @Test
    void shouldReturn200WhenGroupIsUpdatedSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();
        UpdateDeviceGroupRequest request = new UpdateDeviceGroupRequest("Sensores Planta 2", "Grupo actualizado");
        UpdateDeviceGroupCommand expectedCommand = new UpdateDeviceGroupCommand(groupId, "Sensores Planta 2", "Grupo actualizado");
        GetDeviceGroupResult result = new GetDeviceGroupResult(groupId, "Sensores Planta 2", "Grupo actualizado");

        when(updateDeviceGroupUseCase.update(expectedCommand)).thenReturn(result);

        mockMvc.perform(put("/api/v1/device-groups/{id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sensores Planta 2"));
    }

    @Test
    void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
        UUID groupId = UUID.randomUUID();
        UpdateDeviceGroupRequest request = new UpdateDeviceGroupRequest("", "Grupo actualizado");

        mockMvc.perform(put("/api/v1/device-groups/{id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn204WhenGroupIsDeleted() throws Exception {
        UUID groupId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/device-groups/{id}", groupId))
                .andExpect(status().isNoContent());

        verify(deleteDeviceGroupUseCase).delete(new DeleteDeviceGroupCommand(groupId));
    }

    @Test
    void shouldReturn409WhenGroupStillHasDevices() throws Exception {
        UUID groupId = UUID.randomUUID();

        doThrow(new DeviceGroupNotEmptyException()).when(deleteDeviceGroupUseCase)
                .delete(new DeleteDeviceGroupCommand(groupId));

        mockMvc.perform(delete("/api/v1/device-groups/{id}", groupId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERR-011"));
    }

    @Test
    void shouldReturn201WhenDeviceIsAddedToGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        AddDeviceToGroupResult result = new AddDeviceToGroupResult(UUID.randomUUID(), deviceId, groupId, Instant.now());

        when(addDeviceToGroupUseCase.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId))).thenReturn(result);

        mockMvc.perform(post("/api/v1/device-groups/{groupId}/devices/{deviceId}", groupId, deviceId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    }

    @Test
    void shouldReturn409WhenDeviceAlreadyInGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        when(addDeviceToGroupUseCase.addToGroup(new AddDeviceToGroupCommand(deviceId, groupId)))
                .thenThrow(new DeviceAlreadyInGroupException());

        mockMvc.perform(post("/api/v1/device-groups/{groupId}/devices/{deviceId}", groupId, deviceId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ERR-008"));
    }

    @Test
    void shouldReturn204WhenDeviceIsRemovedFromGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/device-groups/{groupId}/devices/{deviceId}", groupId, deviceId))
                .andExpect(status().isNoContent());

        verify(removeDeviceFromGroupUseCase).remove(new RemoveDeviceFromGroupCommand(deviceId, groupId));
    }

    @Test
    void shouldReturn404WhenDeviceIsNotInGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        doThrow(new DeviceNotInGroupException()).when(removeDeviceFromGroupUseCase)
                .remove(new RemoveDeviceFromGroupCommand(deviceId, groupId));

        mockMvc.perform(delete("/api/v1/device-groups/{groupId}/devices/{deviceId}", groupId, deviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-010"));
    }
}
