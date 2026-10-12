package com.pulsegrid.queryapi.infrastructure.security;

import com.pulsegrid.queryapi.application.port.in.AlertQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.NotificationQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.TelemetryQueryUseCase;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.jwt.secret=" + GraphQlEndpointSecurityTest.SECRET)
class GraphQlEndpointSecurityTest {

    static final String SECRET = "test-secret-key-with-at-least-32-bytes!!";

    private static final String BODY = "{\"query\":\"{ deviceGroups { name } }\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceQueryUseCase deviceQueryUseCase;

    @MockitoBean
    private TelemetryQueryUseCase telemetryQueryUseCase;

    @MockitoBean
    private AlertQueryUseCase alertQueryUseCase;

    @MockitoBean
    private NotificationQueryUseCase notificationQueryUseCase;

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenIsSignedWithAnotherKey() throws Exception {
        String forged = token("VIEWER", "another-secret-key-with-at-least-32-bytes");

        mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content(BODY)
                        .header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExecuteQueryForViewerRole() throws Exception {
        when(deviceQueryUseCase.listGroups()).thenReturn(List.of());

        MvcResult pending = mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content(BODY)
                        .header("Authorization", "Bearer " + token("VIEWER", SECRET)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deviceGroups").isEmpty());
    }

    @Test
    void shouldExposeHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    private String token(String role, String secret) {
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
