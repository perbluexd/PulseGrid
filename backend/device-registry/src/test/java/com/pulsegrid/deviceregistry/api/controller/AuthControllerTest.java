package com.pulsegrid.deviceregistry.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.api.dto.auth.LoginRequest;
import com.pulsegrid.deviceregistry.api.mapper.LoginResponseMapper;
import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.error.DashboardUserInactiveException;
import com.pulsegrid.deviceregistry.application.error.InvalidCredentialsException;
import com.pulsegrid.deviceregistry.application.port.in.LoginDashboardUserUseCase;
import com.pulsegrid.deviceregistry.application.port.result.LoginDashboardUserResult;
import com.pulsegrid.deviceregistry.domain.model.Role;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, InternalServiceAuthenticationFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
@Import(LoginResponseMapper.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginDashboardUserUseCase loginDashboardUserUseCase;

    @Test
    void shouldReturn200WithTokenWhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("percy@gmail.com", "percy123");
        LoginDashboardUserResult result = new LoginDashboardUserResult(
                "fake-jwt-token", 1800L, UUID.randomUUID(), "percy@gmail.com", Role.ADMIN);
        LoginDashboardUserCommand expectedCommand = new LoginDashboardUserCommand("percy@gmail.com", "percy123");

        when(loginDashboardUserUseCase.login(expectedCommand)).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("fake-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.email").value("percy@gmail.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "percy123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn400WhenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("percy@gmail.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR-000"));
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("percy@gmail.com", "wrong-password");

        when(loginDashboardUserUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERR-002"));
    }

    @Test
    void shouldReturn403WhenDashboardUserIsInactive() throws Exception {
        LoginRequest request = new LoginRequest("percy@gmail.com", "percy123");
        UUID userId = UUID.randomUUID();

        when(loginDashboardUserUseCase.login(any())).thenThrow(new DashboardUserInactiveException(userId.toString()));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERR-003"));
    }
}
