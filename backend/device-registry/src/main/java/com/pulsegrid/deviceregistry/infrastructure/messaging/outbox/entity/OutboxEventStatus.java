package com.pulsegrid.deviceregistry.infrastructure.messaging.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
