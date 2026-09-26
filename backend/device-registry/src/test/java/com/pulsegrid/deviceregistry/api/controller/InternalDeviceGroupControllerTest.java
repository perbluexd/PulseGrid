package com.pulsegrid.deviceregistry.api.controller;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceGroupNotFoundException;
import com.pulsegrid.deviceregistry.application.port.in.GetDeviceGroupUseCase;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupResult;
import com.pulsegrid.deviceregistry.infrastructure.security.InternalServiceAuthenticationFilter;
import com.pulsegrid.deviceregistry.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InternalDeviceGroupController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, InternalServiceAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class InternalDeviceGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDeviceGroupUseCase getDeviceGroupUseCase;

    @Test
    void shouldReturn200WithGroupWhenGroupExists() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(getDeviceGroupUseCase.get(new GetDeviceGroupCommand(groupId)))
                .thenReturn(new GetDeviceGroupResult(groupId, "Planta Norte", "Sensores de la planta norte"));

        mockMvc.perform(get("/api/v1/internal/device-groups/{id}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.name").value("Planta Norte"));
    }

    @Test
    void shouldReturn404WhenGroupDoesNotExist() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(getDeviceGroupUseCase.get(any())).thenThrow(new DeviceGroupNotFoundException(groupId.toString()));

        mockMvc.perform(get("/api/v1/internal/device-groups/{id}", groupId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ERR-007"));
    }
}
