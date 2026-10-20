package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox;

import com.pulsegrid.deviceregistry.application.event.ApiKeyRotatedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceGroupMembershipChangedEvent;
import com.pulsegrid.deviceregistry.application.event.DeviceStatusChangedEvent;

public enum OutboxEventType {
    API_KEY_ROTATED(ApiKeyRotatedEvent.class),
    MEMBERSHIP_CHANGED(DeviceGroupMembershipChangedEvent.class),
    DEVICE_STATUS_CHANGED(DeviceStatusChangedEvent.class);

    private final Class<?> eventClass;

    OutboxEventType(Class<?> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<?> eventClass() {
        return eventClass;
    }
}
