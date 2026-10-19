package com.pulsegrid.deviceregistry.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceRegistryEventKafkaPublisherTest {

    private static final String TOPIC = "device-registry-events";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private DeviceRegistryEventKafkaPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DeviceRegistryEventKafkaPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    void shouldReturnFailedFutureWhenKafkaTemplateThrowsSynchronously() {
        Object event = new Object();
        KafkaException failure = new KafkaException("Send failed");
        when(kafkaTemplate.send(TOPIC, "device-1", event)).thenThrow(failure);

        CompletableFuture<SendResult<String, Object>> result = publisher.send("device-1", event);

        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(failure);
    }
}
