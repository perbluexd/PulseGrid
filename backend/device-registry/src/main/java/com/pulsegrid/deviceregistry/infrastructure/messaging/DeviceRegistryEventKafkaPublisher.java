package com.pulsegrid.deviceregistry.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DeviceRegistryEventKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public DeviceRegistryEventKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                             @Value("${app.kafka.device-registry-events-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public CompletableFuture<SendResult<String, Object>> send(String key, Object event) {
        try {
            return kafkaTemplate.send(topic, key, event);
        } catch (RuntimeException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }
}
