package com.pulsegrid.notificationworkers.infrastructure.persistence.adapter;

import com.pulsegrid.notificationworkers.application.port.out.ProcessedNotificationRepositoryPort;
import com.pulsegrid.notificationworkers.infrastructure.persistence.repository.ProcessedNotificationJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Component
public class ProcessedNotificationRepositoryAdapter implements ProcessedNotificationRepositoryPort {

    private final ProcessedNotificationJpaRepository processedNotificationJpaRepository;

    @Override
    public boolean markAsProcessed(UUID alertId, Instant processedAt) {
        return processedNotificationJpaRepository.insertIfAbsent(alertId, processedAt) == 1;
    }
}
