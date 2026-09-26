package com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.config.RabbitMqProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = {RabbitMqConfig.class, AlertNotificationRabbitPublisherAdapter.class},
        properties = {
                "spring.rabbitmq.publisher-confirm-type=correlated",
                "spring.rabbitmq.publisher-returns=true",
                "spring.rabbitmq.template.mandatory=true",
                "app.rabbitmq.alert-notifications.exchange=alert-notifications.exchange",
                "app.rabbitmq.alert-notifications.queue=alert-notifications",
                "app.rabbitmq.alert-notifications.routing-key=alert.triggered",
                "app.rabbitmq.alert-notifications.dead-letter-exchange=alert-notifications.dlx",
                "app.rabbitmq.alert-notifications.dead-letter-queue=alert-notifications.dlq",
                "app.rabbitmq.alert-notifications.confirm-timeout=5s"
        }
)
@ImportAutoConfiguration({RabbitAutoConfiguration.class, JacksonAutoConfiguration.class})
@Testcontainers
class AlertNotificationRabbitPublisherAdapterIT {

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    @Autowired
    private AlertNotificationRabbitPublisherAdapter publisherAdapter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMqProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPublishPersistentJsonMessageToAlertNotificationsQueue() throws Exception {
        AlertTriggeredNotification notification = notification();

        publisherAdapter.publish(notification);

        Message message = rabbitTemplate.receive(properties.queue(), 5000);
        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getReceivedDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);

        JsonNode body = objectMapper.readTree(message.getBody());
        assertThat(body.get("alertId").asText()).isEqualTo(notification.alertId().toString());
        assertThat(body.get("deviceName").asText()).isEqualTo("Horno 3");
        assertThat(body.get("severity").asText()).isEqualTo("CRITICAL");
    }

    @Test
    void shouldFailWhenMessageCannotBeRoutedToAnyQueue() {
        RabbitMqProperties unroutable = new RabbitMqProperties(properties.exchange(), properties.queue(),
                "routing.key.sin.binding", properties.deadLetterExchange(), properties.deadLetterQueue(),
                Duration.ofSeconds(5));
        AlertNotificationRabbitPublisherAdapter adapter = new AlertNotificationRabbitPublisherAdapter(rabbitTemplate, unroutable);

        assertThatThrownBy(() -> adapter.publish(notification()))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("no pudo enrutar");
    }

    private AlertTriggeredNotification notification() {
        return new AlertTriggeredNotification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Horno 3",
                "TEMPERATURE", AlertCondition.GREATER_THAN, 80, 92.5, AlertSeverity.CRITICAL, Instant.now());
    }
}
