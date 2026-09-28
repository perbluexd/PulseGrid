package com.pulsegrid.alertingservice.infrastructure.messaging.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
