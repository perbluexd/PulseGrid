package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceStatusChangedEvent;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRegistryEventPublisherPort;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity.OutboxEventEntity;
import com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OutboxDeviceRegistryEventPublisherAdapter implements DeviceRegistryEventPublisherPort {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void publishApiKeyRotated(ApiKeyRotatedEvent event) {
        store(event.deviceId(), OutboxEventType.API_KEY_ROTATED, event);
    }

    @Override
    public void publishMembershipChanged(DeviceGroupMembershipChangedEvent event) {
        store(event.deviceId(), OutboxEventType.MEMBERSHIP_CHANGED, event);
    }

    @Override
    public void publishDeviceStatusChanged(DeviceStatusChangedEvent event) {
        store(event.deviceId(), OutboxEventType.DEVICE_STATUS_CHANGED, event);
    }

    private void store(UUID deviceId, OutboxEventType type, Object event) {
        repository.save(OutboxEventEntity.pending(deviceId, type.name(), toJson(deviceId, event)));
    }

    private String toJson(UUID deviceId, Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el evento del dispositivo " + deviceId, ex);
        }
    }
}
