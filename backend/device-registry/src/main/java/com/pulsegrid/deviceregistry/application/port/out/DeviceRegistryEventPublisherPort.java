package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;

public interface DeviceRegistryEventPublisherPort {
    void publishApiKeyRotated(ApiKeyRotatedEvent event);
}
