package com.pulsegrid.deviceregistry.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.api.dto.device.RegisterDeviceRequest;
import com.pulsegrid.deviceregistry.api.mapper.GetDeviceResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.ListDevicesResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.RegisterDeviceRequestMapper;
import com.pulsegrid.deviceregistry.api.mapper.RegisterDeviceResponseMapper;
import com.pulsegrid.deviceregistry.api.mapper.RotateApiKeyResponseMapper;
import com.pulsegrid.deviceregistry.application.command.DeactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.DecommissionDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.GetDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.ReactivateDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.RegisterDeviceCommand;
import com.pulsegrid.deviceregistry.application.command.RotateDeviceApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.ActiveApiKeyNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.DeactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.DecommissionDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ListDevicesUseCase;
import com.pulsegrid.deviceregistry.application.port.in.ReactivateDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RegisterDeviceUseCase;
import com.pulsegrid.deviceregistry.application.port.in.RotateDeviceApiKeyUseCase;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceResult;
import com.pulsegrid.deviceregistry.application.port.result.ListDevicesResult;
import com.pulsegrid.deviceregistry.application.port.result.RegisterDeviceResult;
import com.pulsegrid.deviceregistry.application.port.result.RotateDeviceApiKeyResult;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DeviceController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, InternalServiceAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
@Import({RegisterDeviceRequestMapper.class, RegisterDeviceResponseMapper.class, GetDeviceResponseMapper.class,
        ListDevicesResponseMapper.class, RotateApiKeyResponseMapper.class})
class  DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterDeviceUseCase registerDeviceUseCase;

    @MockitoBean
    private GetDeviceUseCase getDeviceUseCase;

    @MockitoBean
    private ListDevicesUseCase listDevicesUseCase;

    @MockitoBean
    private DeactivateDeviceUseCase deactivateDeviceUseCase;

    @MockitoBean
    private ReactivateDeviceUseCase reactivateDeviceUseCase;

    @MockitoBean
    private DecommissionDeviceUseCase decommissionDeviceUseCase;

    @MockitoBean
    private RotateDeviceApiKeyUseCase rotateDeviceApiKeyUseCase;

    @Test
    void shouldReturn201WhenDeviceIsRegisteredSuccessfully() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest("Sensor Temperatura", "SENSOR", "Planta 1");
        RegisterDeviceCommand expectedCommand = new RegisterDeviceCommand("Sensor Temperatura", DeviceType.SENSOR, "Planta 1");
        RegisterDeviceResult result = new RegisterDeviceResult(
                UUID.randomUUID(), "Sensor Temperatura", DeviceType.SENSOR, DeviceStatus.ACTIVE, "raw-api-key");

        when(registerDeviceUseCase.register(expectedCommand)).thenReturn(result);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sensor Temperatura"))
                .andExpect(jsonPath("$.type").value("SENSOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.apiKey").value("raw-api-key"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest("", "SENSOR", "Planta 1");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn400WhenTypeIsInvalid() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest("Sensor Temperatura", "DRONE", "Planta 1");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));

        verify(registerDeviceUseCase, never()).register(any());
    }

    @Test
    void shouldReturn200WithDeviceWhenItExists() throws Exception {
        UUID deviceId = UUID.randomUUID();
        Instant now = Instant.now();
        GetDeviceResult result = new GetDeviceResult(deviceId, "Sensor Temperatura", DeviceType.SENSOR,
                DeviceStatus.ACTIVE, "Planta 1", now, now, now);

        when(getDeviceUseCase.get(new GetDeviceCommand(deviceId))).thenReturn(result);

        mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value("Sensor Temperatura"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenDeviceDoesNotExist() throws Exception {
        UUID deviceId = UUID.randomUUID();

        when(getDeviceUseCase.get(new GetDeviceCommand(deviceId))).thenThrow(new DeviceNotFoundException(deviceId.toString()));

        mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-006"));
    }

    @Test
    void shouldReturn200WithAllDevices() throws Exception {
        Instant now = Instant.now();
        GetDeviceResult device = new GetDeviceResult(UUID.randomUUID(), "Sensor Temperatura", DeviceType.SENSOR,
                DeviceStatus.ACTIVE, "Planta 1", now, now, now);
        ListDevicesResult result = new ListDevicesResult(List.of(device));

        when(listDevicesUseCase.listAll()).thenReturn(result);

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devices", hasSize(1)))
                .andExpect(jsonPath("$.devices[0].name").value("Sensor Temperatura"));
    }

    @Test
    void shouldReturn204WhenDeviceIsDeactivated() throws Exception {
        UUID deviceId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/devices/{id}/deactivate", deviceId))
                .andExpect(status().isNoContent());

        verify(deactivateDeviceUseCase).deactivate(new DeactivateDeviceCommand(deviceId));
    }

    @Test
    void shouldReturn204WhenDeviceIsReactivated() throws Exception {
        UUID deviceId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/devices/{id}/reactivate", deviceId))
                .andExpect(status().isNoContent());

        verify(reactivateDeviceUseCase).reactivate(new ReactivateDeviceCommand(deviceId));
    }

    @Test
    void shouldReturn204WhenDeviceIsDecommissioned() throws Exception {
        UUID deviceId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/devices/{id}/decommission", deviceId))
                .andExpect(status().isNoContent());

        verify(decommissionDeviceUseCase).decommission(new DecommissionDeviceCommand(deviceId));
    }

    @Test
    void shouldReturn200WithNewApiKeyWhenRotationSucceeds() throws Exception {
        UUID deviceId = UUID.randomUUID();
        RotateDeviceApiKeyResult result = new RotateDeviceApiKeyResult(deviceId, UUID.randomUUID(), UUID.randomUUID(), "new-raw-key");

        when(rotateDeviceApiKeyUseCase.rotate(new RotateDeviceApiKeyCommand(deviceId))).thenReturn(result);

        mockMvc.perform(patch("/api/v1/devices/{id}/api-key/rotate", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newApiKey").value("new-raw-key"));
    }

    @Test
    void shouldReturn404WhenDeviceHasNoActiveApiKeyToRotate() throws Exception {
        UUID deviceId = UUID.randomUUID();

        when(rotateDeviceApiKeyUseCase.rotate(new RotateDeviceApiKeyCommand(deviceId))).thenThrow(new ActiveApiKeyNotFoundException());

        mockMvc.perform(patch("/api/v1/devices/{id}/api-key/rotate", deviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-009"));
    }
}
