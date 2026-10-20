package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import(OutboxDeviceRegistryEventPublisherAdapter.class)
@Testcontainers
class OutboxDeviceRegistryEventPublisherAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private OutboxDeviceRegistryEventPublisherAdapter adapter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldStorePendingEventWithDeviceIdAsAggregateWhenTransactionCommits() throws Exception {
        ApiKeyRotatedEvent event = event();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> adapter.publishApiKeyRotated(event));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT event_type, status, attempts, payload::text AS payload FROM outbox_events WHERE aggregate_id = ?",
                event.deviceId());
        assertThat(row.get("event_type")).isEqualTo(OutboxEventType.API_KEY_ROTATED.name());
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat(row.get("attempts")).isEqualTo(0);

        JsonNode payload = objectMapper.readTree((String) row.get("payload"));
        assertThat(payload.get("deviceId").asText()).isEqualTo(event.deviceId().toString());
        assertThat(payload.get("revokedApiKeyHash").asText()).isEqualTo("revoked-hash");
    }

    @Test
    void shouldNotLeaveAnyEventWhenTransactionRollsBack() {
        ApiKeyRotatedEvent event = event();

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.publishApiKeyRotated(event);
            throw new IllegalStateException("Falla después de publicar, antes del commit");
        })).isInstanceOf(IllegalStateException.class);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ?", Integer.class, event.deviceId());
        assertThat(count).isZero();
    }

    @Test
    void shouldRejectPublishOutsideTransaction() {
        assertThatThrownBy(() -> adapter.publishApiKeyRotated(event()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private ApiKeyRotatedEvent event() {
        return new ApiKeyRotatedEvent(UUID.randomUUID(), UUID.randomUUID(), "revoked-hash", UUID.randomUUID(), Instant.now());
    }
}
