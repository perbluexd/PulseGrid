package com.pulsegrid.deviceregistry.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.api.dto.internal.ResolveByApiKeyRequest;
import com.pulsegrid.deviceregistry.api.mapper.DeviceGroupsResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ResolveByApiKeyResponseMapper;
import com.pulsegrid.deviceregistry.application.command.GetDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.error.InvalidApiKeyException;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupsUseCase;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ResolveDeviceByApiKeyUseCase;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupsResult;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;
import com.pulsegrid.deviceregistry.application.port.result.ResolveDeviceByApiKeyResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InternalDeviceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, InternalServiceAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
@Import({ResolveByApiKeyResponseMapper.class, DeviceGroupsResponseMapper.class})
class InternalDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResolveDeviceByApiKeyUseCase resolveDeviceByApiKeyUseCase;

    @MockitoBean
    private GetDeviceGroupsUseCase getDeviceGroupsUseCase;

    @MockitoBean
    private GetDeviceUseCase getDeviceUseCase;

    @Test
    void shouldReturn200WithDeviceWhenApiKeyIsValid() throws Exception {
        ResolveByApiKeyRequest request = new ResolveByApiKeyRequest("raw-api-key");
        ResolveDeviceByApiKeyResult result = new ResolveDeviceByApiKeyResult(UUID.randomUUID(), "Sensor Temperatura",
                DeviceType.SENSOR, DeviceStatus.ACTIVE);

        when(resolveDeviceByApiKeyUseCase.resolve(new ResolveDeviceByApiKeyCommand("raw-api-key"))).thenReturn(result);

        mockMvc.perform(post("/api/v1/internal/devices/resolve-by-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sensor Temperatura"));
    }

    @Test
    void shouldReturn400WhenApiKeyIsBlank() throws Exception {
        ResolveByApiKeyRequest request = new ResolveByApiKeyRequest("");

        mockMvc.perform(post("/api/v1/internal/devices/resolve-by-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn401WhenApiKeyIsInvalid() throws Exception {
        ResolveByApiKeyRequest request = new ResolveByApiKeyRequest("wrong-key");

        when(resolveDeviceByApiKeyUseCase.resolve(any())).thenThrow(new InvalidApiKeyException());

        mockMvc.perform(post("/api/v1/internal/devices/resolve-by-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERR-004"));
    }

    @Test
    void shouldReturn200WithGroupIdsWhenDeviceExists() throws Exception {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        GetDeviceGroupsResult result = new GetDeviceGroupsResult(deviceId, List.of(groupId));

        when(getDeviceGroupsUseCase.getGroups(new GetDeviceGroupsCommand(deviceId))).thenReturn(result);

        mockMvc.perform(get("/api/v1/internal/devices/{id}/groups", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupIds", hasSize(1)))
                .andExpect(jsonPath("$.groupIds[0]").value(groupId.toString()));
    }

    @Test
    void shouldReturn404WhenDeviceDoesNotExistForGroups() throws Exception {
        UUID deviceId = UUID.randomUUID();

        when(getDeviceGroupsUseCase.getGroups(new GetDeviceGroupsCommand(deviceId)))
                .thenThrow(new DeviceNotFoundException(deviceId.toString()));

        mockMvc.perform(get("/api/v1/internal/devices/{id}/groups", deviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-006"));
    }

    @Test
    void shouldReturn200WithDeviceNameAndStatusWhenDeviceExists() throws Exception {
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        GetDeviceResult result = new GetDeviceResult(deviceId, "Sensor Temperatura", DeviceType.SENSOR,
                DeviceStatus.ACTIVE, "Planta 1", now, now, null);

        when(getDeviceUseCase.get(new GetDeviceCommand(deviceId))).thenReturn(result);

        mockMvc.perform(get("/api/v1/internal/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value("Sensor Temperatura"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenGettingUnknownDevice() throws Exception {
        UUID deviceId = UUID.randomUUID();

        when(getDeviceUseCase.get(any())).thenThrow(new DeviceNotFoundException(deviceId.toString()));

        mockMvc.perform(get("/api/v1/internal/devices/{id}", deviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-006"));
    }
}
