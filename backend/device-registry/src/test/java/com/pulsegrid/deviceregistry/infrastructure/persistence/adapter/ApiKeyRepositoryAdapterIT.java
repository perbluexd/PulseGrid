package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.ApiKeyEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.ApiKeyMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.ApiKeyJpaRepository;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceJpaRepository;
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
@Import({ApiKeyRepositoryAdapter.class, ApiKeyMapper.class})
@Testcontainers
class ApiKeyRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ApiKeyRepositoryAdapter apiKeyRepositoryAdapter;

    @Autowired
    private ApiKeyJpaRepository apiKeyJpaRepository;

    @Autowired
    private DeviceJpaRepository deviceJpaRepository;

    private UUID seedDevice() {
        UUID deviceId = UUID.randomUUID();
        deviceJpaRepository.save(new DeviceEntity(deviceId, "Device", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Zone", Instant.now(), Instant.now(), null));
        return deviceId;
    }

    @Test
    void shouldSaveApiKeyAndPersistItInDatabase() {
        UUID deviceId = seedDevice();
        ApiKey apiKey = new ApiKey(UUID.randomUUID(), deviceId, "hash-001", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);

        ApiKey result = apiKeyRepositoryAdapter.save(apiKey);
        apiKeyJpaRepository.flush();

        Optional<ApiKeyEntity> persisted = apiKeyJpaRepository.findById(result.getId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getKeyHash()).isEqualTo("hash-001");
        assertThat(persisted.get().getStatus()).isEqualTo(ApiKeyStatus.ACTIVE);
    }

    @Test
    void shouldFindApiKeyByHashWhenItExists() {
        UUID id = UUID.randomUUID();
        ApiKeyEntity entity = new ApiKeyEntity(id, seedDevice(), "hash-002", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);
        apiKeyJpaRepository.save(entity);

        Optional<ApiKey> result = apiKeyRepositoryAdapter.findByHash("hash-002");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getKeyHash()).isEqualTo("hash-002");
    }

    @Test
    void shouldReturnEmptyWhenHashDoesNotExist() {
        Optional<ApiKey> result = apiKeyRepositoryAdapter.findByHash("does-not-exist");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindActiveApiKeyByDeviceId() {
        UUID deviceId = seedDevice();
        ApiKeyEntity entity = new ApiKeyEntity(UUID.randomUUID(), deviceId, "hash-003", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);
        apiKeyJpaRepository.save(entity);

        Optional<ApiKey> result = apiKeyRepositoryAdapter.findActiveByDeviceId(deviceId);

        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo(deviceId);
        assertThat(result.get().getStatus()).isEqualTo(ApiKeyStatus.ACTIVE);
    }

    @Test
    void shouldReturnEmptyWhenNoActiveApiKeyForDevice() {
        UUID deviceId = seedDevice();
        ApiKeyEntity revoked = new ApiKeyEntity(UUID.randomUUID(), deviceId, "hash-004", ApiKeyStatus.REVOKED,
                Instant.now(), null, Instant.now());
        apiKeyJpaRepository.save(revoked);

        Optional<ApiKey> result = apiKeyRepositoryAdapter.findActiveByDeviceId(deviceId);

        assertThat(result).isEmpty();
    }
}
