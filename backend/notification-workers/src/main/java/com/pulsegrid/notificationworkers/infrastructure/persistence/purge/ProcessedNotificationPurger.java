package com.pulsegrid.notificationworkers.infrastructure.persistence.purge;

import com.pulsegrid.notificationworkers.infrastructure.persistence.repository.ProcessedNotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedNotificationPurger {

    private final ProcessedNotificationJpaRepository processedNotificationJpaRepository;
    private final ProcessedNotificationPurgeProperties properties;

    @Scheduled(cron = "${app.processed-notifications.purge-cron}")
    @Transactional
    public void purgeExpired() {
        Instant processedBefore = Instant.now().minus(properties.retention());
        int deleted = processedNotificationJpaRepository.deleteByProcessedAtBefore(processedBefore);
        log.info("Purgados {} registros de notificaciones procesadas", deleted);
    }
}
