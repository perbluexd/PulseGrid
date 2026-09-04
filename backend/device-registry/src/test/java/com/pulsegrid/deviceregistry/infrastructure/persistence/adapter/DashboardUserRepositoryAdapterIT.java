package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;
import com.pulsegrid.deviceregistry.domain.model.Role;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DashboardUserEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DashboardUserMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DashboardUserJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DashboardUserRepositoryAdapter.class, DashboardUserMapper.class})
@Testcontainers
class DashboardUserRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DashboardUserRepositoryAdapter dashboardUserRepositoryAdapter;

    @Autowired
    private DashboardUserJpaRepository dashboardUserJpaRepository;

    @Test
    void shouldFindDashboardUserByEmailWhenItExists() {
        DashboardUserEntity entity = new DashboardUserEntity(UUID.randomUUID(), "viewer@pulsegrid.io",
                "hashed-pw", Role.ADMIN, true, Instant.now(), Instant.now());
        dashboardUserJpaRepository.save(entity);

        Optional<DashboardUser> result = dashboardUserRepositoryAdapter.findByEmail(new Email("viewer@pulsegrid.io"));

        assertThat(result).isPresent();
        assertThat(result.get().getEmail().getValue()).isEqualTo("viewer@pulsegrid.io");
        assertThat(result.get().getPasswordHash()).isEqualTo("hashed-pw");
        assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<DashboardUser> result = dashboardUserRepositoryAdapter.findByEmail(new Email("nadie@gmail.com"));

        assertThat(result).isEmpty();
    }
}
