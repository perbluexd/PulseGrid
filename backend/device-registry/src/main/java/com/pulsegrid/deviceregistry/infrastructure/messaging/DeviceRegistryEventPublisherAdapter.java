package com.pulsegrid.deviceregistry.infrastructure.messaging;

import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeviceRegistryEventPublisherAdapter implements DeviceRegistryEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public DeviceRegistryEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate,
                                                @Value("${app.kafka.device-registry-events-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishApiKeyRotated(ApiKeyRotatedEvent event) {
        kafkaTemplate.send(topic, event.deviceId().toString(), event);
    }

    @Override
    public void publishMembershipChanged(DeviceGroupMembershipChangedEvent event) {
        kafkaTemplate.send(topic, event.deviceId().toString(), event);
    }
}
