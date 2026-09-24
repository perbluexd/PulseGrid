package com.pulsegrid.aggregationservice.infrastructure.messaging;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class TelemetryReadingEventConsumerIT {

    private static final String TOPIC = "telemetry-events";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
            DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("app.kafka.telemetry-events-topic", () -> TOPIC);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistTelemetryReadingWhenEventArrivesFromKafka() throws Exception {
        UUID deviceId = UUID.randomUUID();
        String json = """
                {"deviceId":"%s","metricType":"TEMPERATURE","value":23.5,"recordedAt":"2026-09-20T10:15:30Z"}
                """.formatted(deviceId);

        publish(deviceId.toString(), json);

        Integer count = 0;
        for (int attempt = 0; attempt < 20 && count == 0; attempt++) {
            Thread.sleep(500);
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM telemetry_reading WHERE device_id = ?", Integer.class, deviceId);
        }
        assertThat(count).isEqualTo(1);
    }

    private void publish(String key, String value) throws Exception {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>(TOPIC, key, value)).get();
        }
    }
}
