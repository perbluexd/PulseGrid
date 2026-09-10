package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;

public interface DeviceRegistryEventPublisherPort {
    void publishApiKeyRotated(ApiKeyRotatedEvent event);

    void publishMembershipChanged(DeviceGroupMembershipChangedEvent event);
}
