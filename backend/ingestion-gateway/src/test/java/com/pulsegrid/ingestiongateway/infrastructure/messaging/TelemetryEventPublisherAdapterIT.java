package com.pulsegrid.ingestiongateway.infrastructure.messaging;

import com.pulsegrid.ingestiongateway.application.event.TelemetryReadingEvent;
import com.pulsegrid.ingestiongateway.infrastructure.messaging.config.KafkaProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {KafkaProducerConfig.class, TelemetryEventPublisherAdapter.class, JacksonAutoConfiguration.class})
@Testcontainers
class TelemetryEventPublisherAdapterIT {

    private static final String TOPIC = "telemetry-events";

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("app.kafka.telemetry-events-topic", () -> TOPIC);
    }

    @Autowired
    private TelemetryEventPublisherAdapter telemetryEventPublisherAdapter;

    @Test
    void shouldPublishTelemetryReadingEventToTopicWithDeviceIdAsKey() {
        UUID deviceId = UUID.randomUUID();
        TelemetryReadingEvent event = new TelemetryReadingEvent(
                deviceId, "TEMPERATURE", 23.5, Instant.parse("2026-09-20T10:15:30Z"));

        telemetryEventPublisherAdapter.publish(event).block();

        ConsumerRecord<String, String> record = consumeOneRecord();

        assertThat(record.key()).isEqualTo(deviceId.toString());
        assertThat(record.value()).contains(deviceId.toString());
        assertThat(record.value()).contains("TEMPERATURE");
    }

    private ConsumerRecord<String, String> consumeOneRecord() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(TOPIC));

            ConsumerRecords<String, String> records = ConsumerRecords.empty();
            for (int attempt = 0; attempt < 10 && records.isEmpty(); attempt++) {
                records = consumer.poll(Duration.ofSeconds(1));
            }
            return records.iterator().next();
        }
    }
}
