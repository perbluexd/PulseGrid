package com.pulsegrid.notificationworkers.infrastructure.persistence.adapter;

import com.pulsegrid.notificationworkers.application.port.out.SentNotification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProcessedNotificationRepositoryAdapter.class, SentNotificationQueryAdapter.class})
@Testcontainers
class SentNotificationQueryAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProcessedNotificationRepositoryAdapter processedNotificationRepositoryAdapter;

    @Autowired
    private SentNotificationQueryAdapter sentNotificationQueryAdapter;

    @Test
    void shouldReturnOnlyTheRequestedAlertsThatWereNotified() {
        UUID notified = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        UUID neverNotified = UUID.randomUUID();
        Instant notifiedAt = Instant.parse("2026-10-08T20:00:00Z");
        processedNotificationRepositoryAdapter.markAsProcessed(notified, notifiedAt);
        processedNotificationRepositoryAdapter.markAsProcessed(other, notifiedAt);

        var result = sentNotificationQueryAdapter.findByAlertIds(List.of(notified, neverNotified));

        assertThat(result).containsExactly(new SentNotification(notified, notifiedAt));
    }

    @Test
    void shouldReturnMostRecentNotificationsFirstUpToTheLimit() {
        UUID oldest = UUID.randomUUID();
        UUID middle = UUID.randomUUID();
        UUID newest = UUID.randomUUID();
        processedNotificationRepositoryAdapter.markAsProcessed(oldest, Instant.parse("2026-10-08T18:00:00Z"));
        processedNotificationRepositoryAdapter.markAsProcessed(middle, Instant.parse("2026-10-08T19:00:00Z"));
        processedNotificationRepositoryAdapter.markAsProcessed(newest, Instant.parse("2026-10-08T20:00:00Z"));

        var result = sentNotificationQueryAdapter.findMostRecent(2);

        assertThat(result).extracting(SentNotification::alertId).containsExactly(newest, middle);
    }
}
