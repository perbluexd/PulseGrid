package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.config.RabbitMqProperties;
import com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.event.AlertTriggeredNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.retry.initial-interval=100ms",
        "app.processed-notifications.purge-cron=-"
})
@Testcontainers
class AlertNotificationFlowIT {

    private static final int MAILPIT_SMTP_PORT = 1025;
    private static final int MAILPIT_HTTP_PORT = 8025;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4-management-alpine");

    @Container
    static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:latest")
            .withExposedPorts(MAILPIT_SMTP_PORT, MAILPIT_HTTP_PORT);

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", MAILPIT::getHost);
        registry.add("spring.mail.port", () -> MAILPIT.getMappedPort(MAILPIT_SMTP_PORT));
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMqProperties properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearMailbox() throws Exception {
        httpClient.send(HttpRequest.newBuilder(mailpitUri("/api/v1/messages")).DELETE().build(),
                HttpResponse.BodyHandlers.discarding());
    }

    @Test
    void shouldSendASingleEmailEvenWhenTheSameAlertIsDeliveredTwice() throws Exception {
        AlertTriggeredNotification notification = notification();

        rabbitTemplate.convertAndSend(properties.queue(), notification);
        rabbitTemplate.convertAndSend(properties.queue(), notification);

        JsonNode messages = awaitMailboxSize(1);
        assertThat(messages.get("messages").get(0).get("Subject").asText())
                .isEqualTo("[CRITICAL] Alerta en Horno 3: TEMPERATURE");

        Thread.sleep(1000);
        assertThat(mailbox().get("total").asInt()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM processed_notifications WHERE alert_id = ?", Integer.class,
                notification.alertId())).isEqualTo(1);
    }

    @Test
    void shouldRouteMalformedMessagesToTheDeadLetterQueue() {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        rabbitTemplate.send(properties.queue(), new Message("{ json roto".getBytes(), messageProperties));

        Message deadLettered = rabbitTemplate.receive(properties.deadLetterQueue(), 10000);
        assertThat(deadLettered).isNotNull();
        assertThat(new String(deadLettered.getBody())).isEqualTo("{ json roto");
    }

    private JsonNode awaitMailboxSize(int expected) throws Exception {
        for (int attempt = 0; attempt < 30; attempt++) {
            JsonNode mailbox = mailbox();
            if (mailbox.get("total").asInt() >= expected) {
                return mailbox;
            }
            Thread.sleep(500);
        }
        throw new AssertionError("Mailpit no recibió " + expected + " email(s) a tiempo");
    }

    private JsonNode mailbox() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(mailpitUri("/api/v1/messages")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private URI mailpitUri(String path) {
        return URI.create("http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(MAILPIT_HTTP_PORT) + path);
    }

    private AlertTriggeredNotification notification() {
        return new AlertTriggeredNotification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Horno 3",
                "TEMPERATURE", "GREATER_THAN", 80.0, 92.5, "CRITICAL", Instant.parse("2026-10-01T14:00:00Z"));
    }
}
