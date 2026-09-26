package com.pulsegrid.alertingservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceSummary;
import com.pulsegrid.alertingservice.domain.model.AlertCondition;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Testcontainers
class TelemetryAlertFlowIT {

    private static final String TOPIC = "telemetry-events";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @MockitoBean
    private DeviceRegistryPort deviceRegistryPort;

    @Autowired
    private AlertRuleRepositoryPort alertRuleRepositoryPort;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAlertAndPublishToRabbitWhenTelemetryCrossesGroupThreshold() throws Exception {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        AlertRule rule = alertRuleRepositoryPort.save(AlertRule.create(null, groupId, "TEMPERATURE",
                AlertCondition.GREATER_THAN, 80, AlertSeverity.CRITICAL));

        when(deviceRegistryPort.findGroupIds(deviceId)).thenReturn(List.of(groupId));
        when(deviceRegistryPort.findDevice(deviceId)).thenReturn(Optional.of(new DeviceSummary(deviceId, "Horno 3")));

        publish(deviceId, 92.5);

        Message message = rabbitTemplate.receive("alert-notifications", 30000);
        assertThat(message).isNotNull();

        JsonNode body = objectMapper.readTree(message.getBody());
        assertThat(body.get("alertRuleId").asText()).isEqualTo(rule.getId().toString());
        assertThat(body.get("deviceId").asText()).isEqualTo(deviceId.toString());
        assertThat(body.get("deviceName").asText()).isEqualTo("Horno 3");
        assertThat(body.get("triggeredValue").asDouble()).isEqualTo(92.5);

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM alerts WHERE id = ?", String.class, UUID.fromString(body.get("alertId").asText()));
        assertThat(status).isEqualTo("TRIGGERED");
    }

    private void publish(UUID deviceId, double value) throws Exception {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        String json = """
                {"deviceId":"%s","metricType":"TEMPERATURE","value":%s,"recordedAt":"2026-09-26T10:15:30Z"}
                """.formatted(deviceId, value);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>(TOPIC, deviceId.toString(), json)).get();
        }
    }
}
