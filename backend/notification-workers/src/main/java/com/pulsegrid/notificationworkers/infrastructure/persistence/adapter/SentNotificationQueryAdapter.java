package com.pulsegrid.notificationworkers.infrastructure.persistence.adapter;

import com.pulsegrid.notificationworkers.application.port.out.SentNotification;
import com.pulsegrid.notificationworkers.application.port.out.SentNotificationQueryPort;
import com.pulsegrid.notificationworkers.infrastructure.persistence.entity.ProcessedNotificationEntity;
import com.pulsegrid.notificationworkers.infrastructure.persistence.repository.ProcessedNotificationJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class SentNotificationQueryAdapter implements SentNotificationQueryPort {

    private final ProcessedNotificationJpaRepository processedNotificationJpaRepository;

    @Override
    public List<SentNotification> findByAlertIds(List<UUID> alertIds) {
        return processedNotificationJpaRepository.findAllById(alertIds).stream()
                .map(this::toSentNotification)
                .toList();
    }

    @Override
    public List<SentNotification> findMostRecent(int limit) {
        return processedNotificationJpaRepository.findAllByOrderByProcessedAtDesc(Limit.of(limit)).stream()
                .map(this::toSentNotification)
                .toList();
    }

    private SentNotification toSentNotification(ProcessedNotificationEntity entity) {
        return new SentNotification(entity.getAlertId(), entity.getProcessedAt());
    }
}
