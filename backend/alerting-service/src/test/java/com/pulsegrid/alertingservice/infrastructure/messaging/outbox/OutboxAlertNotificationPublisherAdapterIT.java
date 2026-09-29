package com.pulsegrid.alertingservice.infrastructure.messaging.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
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
@Import(OutboxAlertNotificationPublisherAdapter.class)
@Testcontainers
class OutboxAlertNotificationPublisherAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private OutboxAlertNotificationPublisherAdapter adapter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldStorePendingEventWithNotificationPayloadWhenTransactionCommits() throws Exception {
        AlertTriggeredNotification notification = notification();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> adapter.publish(notification));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT event_type, status, attempts, payload::text AS payload FROM outbox_events WHERE aggregate_id = ?",
                notification.alertId());
        assertThat(row.get("event_type")).isEqualTo(OutboxAlertNotificationPublisherAdapter.EVENT_TYPE);
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat(row.get("attempts")).isEqualTo(0);

        JsonNode payload = objectMapper.readTree((String) row.get("payload"));
        assertThat(payload.get("alertId").asText()).isEqualTo(notification.alertId().toString());
        assertThat(payload.get("deviceName").asText()).isEqualTo("Horno 3");
    }

    @Test
    void shouldNotLeaveAnyEventWhenTransactionRollsBack() {
        AlertTriggeredNotification notification = notification();

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            adapter.publish(notification);
            throw new IllegalStateException("Falla después de publicar, antes del commit");
        })).isInstanceOf(IllegalStateException.class);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ?", Integer.class, notification.alertId());
        assertThat(count).isZero();
    }

    @Test
    void shouldRejectPublishOutsideTransaction() {
        assertThatThrownBy(() -> adapter.publish(notification()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private AlertTriggeredNotification notification() {
        return new AlertTriggeredNotification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Horno 3",
                "TEMPERATURE", AlertCondition.GREATER_THAN, 80, 92.5, AlertSeverity.CRITICAL, Instant.now());
    }
}
