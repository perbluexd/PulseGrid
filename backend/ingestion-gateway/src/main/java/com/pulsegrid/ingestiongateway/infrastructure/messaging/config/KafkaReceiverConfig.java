package com.pulsegrid.ingestiongateway.infrastructure.messaging.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Configuration
public class KafkaReceiverConfig {

    private static final String CONSUMER_GROUP_ID = "ingestion-gateway-device-registry-events";

    @Bean
    public KafkaReceiver<String, String> deviceRegistryEventsReceiver(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.device-registry-events-topic}") String topic) {

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.<String, String>create(consumerProps)
                .withKeyDeserializer(new StringDeserializer())
                .withValueDeserializer(new StringDeserializer())
                .commitInterval(Duration.ofSeconds(5))
                .subscription(Collections.singleton(topic));

        return KafkaReceiver.create(receiverOptions);
    }
}
