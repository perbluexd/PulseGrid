package com.pulsegrid.deviceregistry.infrastructure.messaging;

import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.event.MembershipChangeType;
import com.pulsegrid.deviceregistry.infrastructure.messaging.config.KafkaProducerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {KafkaProducerConfig.class, DeviceRegistryEventPublisherAdapter.class})
@Testcontainers
class DeviceRegistryEventPublisherAdapterIT {

    private static final String TOPIC = "device-registry-events";

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("app.kafka.device-registry-events-topic", () -> TOPIC);
    }

    @Autowired
    private DeviceRegistryEventPublisherAdapter deviceRegistryEventPublisherAdapter;

    @Test
    void shouldPublishApiKeyRotatedEventToTopicWithDeviceIdAsKey() {
        UUID deviceId = UUID.randomUUID();
        ApiKeyRotatedEvent event = new ApiKeyRotatedEvent(deviceId, UUID.randomUUID(), "revoked-hash", UUID.randomUUID(), Instant.now());

        deviceRegistryEventPublisherAdapter.publishApiKeyRotated(event);

        ConsumerRecord<String, String> record = consumeOneRecord();

        assertThat(record.key()).isEqualTo(deviceId.toString());
        assertThat(record.value()).contains(deviceId.toString());
    }

    @Test
    void shouldPublishMembershipChangedEventToTopicWithDeviceIdAsKey() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        DeviceGroupMembershipChangedEvent event = new DeviceGroupMembershipChangedEvent(deviceId, groupId, MembershipChangeType.ADDED, Instant.now());

        deviceRegistryEventPublisherAdapter.publishMembershipChanged(event);

        ConsumerRecord<String, String> record = consumeOneRecord();

        assertThat(record.key()).isEqualTo(deviceId.toString());
        assertThat(record.value()).contains(deviceId.toString());
    }

    private ConsumerRecord<String, String> consumeOneRecord() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(KAFKA.getBootstrapServers(), "test-group", "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            return KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10)).iterator().next();
        }
    }
}
