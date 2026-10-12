package com.pulsegrid.notificationworkers.api.controller;

import com.pulsegrid.notificationworkers.application.port.in.GetSentNotificationsUseCase;
import com.pulsegrid.notificationworkers.application.port.out.SentNotification;
import com.pulsegrid.notificationworkers.infrastructure.config.SecurityConfig;
import com.pulsegrid.notificationworkers.infrastructure.security.InternalServiceAuthenticationFilter;
import com.pulsegrid.notificationworkers.infrastructure.security.InternalServiceTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalNotificationController.class)
@Import({SecurityConfig.class, InternalServiceAuthenticationFilter.class, InternalServiceTokenProvider.class})
@TestPropertySource(properties = "app.internal.service-token=test-token")
class InternalNotificationControllerTest {

    private static final String INTERNAL_HEADER = "X-Internal-Service";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetSentNotificationsUseCase getSentNotificationsUseCase;

    @Test
    void shouldReturn401WithoutInternalServiceHeader() throws Exception {
        mockMvc.perform(get("/api/v1/internal/notifications/recent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotificationsForManyAlertsInOneCall() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(getSentNotificationsUseCase.getByAlertIds(List.of(first, second)))
                .thenReturn(List.of(new SentNotification(first, Instant.parse("2026-10-08T20:00:00Z"))));

        mockMvc.perform(get("/api/v1/internal/notifications")
                        .param("alertIds", first + "," + second)
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(1))
                .andExpect(jsonPath("$.notifications[0].alertId").value(first.toString()))
                .andExpect(jsonPath("$.notifications[0].notifiedAt").value("2026-10-08T20:00:00Z"));
    }

    @Test
    void shouldUseDefaultLimitForRecentNotifications() throws Exception {
        when(getSentNotificationsUseCase.getMostRecent(50)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/internal/notifications/recent").header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isEmpty());
        verify(getSentNotificationsUseCase).getMostRecent(50);
    }

    @Test
    void shouldReturn400WhenLimitIsTooHigh() throws Exception {
        mockMvc.perform(get("/api/v1/internal/notifications/recent")
                        .param("limit", "1000")
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isBadRequest());
    }
}
