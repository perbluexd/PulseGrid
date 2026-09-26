package com.pulsegrid.alertingservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.port.out.DeviceGroupCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceRegistryEventConsumer {

    private static final String DEVICE_ID_FIELD = "deviceId";
    private static final String CHANGE_TYPE_FIELD = "changeType";

    private final DeviceGroupCachePort deviceGroupCachePort;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.device-registry-events-topic}")
    public void consume(String message) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(message);
        if (json.hasNonNull(CHANGE_TYPE_FIELD) && json.hasNonNull(DEVICE_ID_FIELD)) {
            deviceGroupCachePort.evict(UUID.fromString(json.get(DEVICE_ID_FIELD).asText()));
        }
    }
}
