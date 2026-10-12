package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.error.DownstreamAccessDeniedException;
import com.pulsegrid.queryapi.application.error.DownstreamServiceUnavailableException;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DeviceRegistryRestClientAdapterTest {

    private MockRestServiceServer server;
    private DeviceRegistryRestClientAdapter adapter;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder()
                .baseUrl("http://device-registry")
                .requestInterceptor(new BearerTokenRelayInterceptor());
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new DeviceRegistryRestClientAdapter(builder.build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-id", "user-jwt", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRelayTheUserTokenAndMapDevices() {
        UUID deviceId = UUID.randomUUID();
        server.expect(requestTo("http://device-registry/api/v1/devices"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer user-jwt"))
                .andRespond(withSuccess("""
                        {"devices":[{"id":"%s","name":"sensor-01","type":"SENSOR","status":"ACTIVE",
                        "location":"Planta Norte","createdAt":"2026-10-01T10:00:00Z","updatedAt":null,"lastSeenAt":null}]}
                        """.formatted(deviceId), MediaType.APPLICATION_JSON));

        var devices = adapter.findAllDevices();

        assertThat(devices).singleElement().satisfies(device -> {
            assertThat(device.id()).isEqualTo(deviceId);
            assertThat(device.status()).isEqualTo(DeviceStatus.ACTIVE);
        });
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenDeviceDoesNotExist() {
        server.expect(requestTo("http://device-registry/api/v1/devices/" + UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.findDevice(UUID.fromString("00000000-0000-0000-0000-000000000001"))).isEmpty();
    }

    @Test
    void shouldTranslateServerErrorsToUnavailable() {
        server.expect(requestTo("http://device-registry/api/v1/device-groups"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.findAllGroups())
                .isInstanceOf(DownstreamServiceUnavailableException.class)
                .hasMessageContaining("device-registry");
    }

    @Test
    void shouldTranslateForbiddenToAccessDenied() {
        server.expect(requestTo("http://device-registry/api/v1/devices"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> adapter.findAllDevices()).isInstanceOf(DownstreamAccessDeniedException.class);
    }
}
