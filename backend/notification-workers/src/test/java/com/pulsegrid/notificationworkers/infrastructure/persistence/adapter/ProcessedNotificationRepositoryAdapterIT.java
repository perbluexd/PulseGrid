package com.pulsegrid.notificationworkers.infrastructure.persistence.adapter;

import com.pulsegrid.notificationworkers.infrastructure.persistence.repository.ProcessedNotificationJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProcessedNotificationRepositoryAdapter.class)
@Testcontainers
class ProcessedNotificationRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProcessedNotificationRepositoryAdapter adapter;

    @Autowired
    private ProcessedNotificationJpaRepository repository;

    @Test
    void shouldMarkAlertAsProcessedOnlyOnce() {
        UUID alertId = UUID.randomUUID();

        assertThat(adapter.markAsProcessed(alertId, Instant.now())).isTrue();
        assertThat(adapter.markAsProcessed(alertId, Instant.now())).isFalse();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void shouldDeleteOnlyRecordsOlderThanTheCutoff() {
        Instant now = Instant.now();
        UUID oldAlertId = UUID.randomUUID();
        UUID recentAlertId = UUID.randomUUID();
        adapter.markAsProcessed(oldAlertId, now.minus(Duration.ofDays(10)));
        adapter.markAsProcessed(recentAlertId, now);

        int deleted = repository.deleteByProcessedAtBefore(now.minus(Duration.ofDays(7)));

        assertThat(deleted).isEqualTo(1);
        assertThat(repository.existsById(oldAlertId)).isFalse();
        assertThat(repository.existsById(recentAlertId)).isTrue();
    }
}
