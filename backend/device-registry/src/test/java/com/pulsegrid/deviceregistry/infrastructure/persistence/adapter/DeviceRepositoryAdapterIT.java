package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceMapper;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DeviceRepositoryAdapter.class, DeviceMapper.class})
@Testcontainers
class DeviceRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DeviceRepositoryAdapter deviceRepositoryAdapter;

    @Autowired
    private DeviceJpaRepository deviceJpaRepository;

    @Test
    void shouldSaveDeviceAndPersistItInDatabase() {
        Device device = new Device(UUID.randomUUID(), "Sensor-01", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Warehouse A", Instant.now(), Instant.now(), null);

        Device result = deviceRepositoryAdapter.save(device);
        deviceJpaRepository.flush();

        Optional<DeviceEntity> persisted = deviceJpaRepository.findById(result.getId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getName()).isEqualTo("Sensor-01");
        assertThat(persisted.get().getType()).isEqualTo(DeviceType.SENSOR);
        assertThat(persisted.get().getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(persisted.get().getLocation()).isEqualTo("Warehouse A");
    }

    @Test
    void shouldFindDeviceByIdWhenItExists() {
        UUID id = UUID.randomUUID();
        DeviceEntity entity = new DeviceEntity(id, "Gateway-01", DeviceType.GATEWAY, DeviceStatus.ACTIVE,
                "Warehouse B", Instant.now(), Instant.now(), null);
        deviceJpaRepository.save(entity);

        Optional<Device> result = deviceRepositoryAdapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Gateway-01");
        assertThat(result.get().getType()).isEqualTo(DeviceType.GATEWAY);
        assertThat(result.get().getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void shouldReturnEmptyWhenDeviceIdDoesNotExist() {
        Optional<Device> result = deviceRepositoryAdapter.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllDevices() {
        DeviceEntity first = new DeviceEntity(UUID.randomUUID(), "Sensor-A", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Zone 1", Instant.now(), Instant.now(), null);
        DeviceEntity second = new DeviceEntity(UUID.randomUUID(), "Controller-A", DeviceType.CONTROLLER, DeviceStatus.INACTIVE,
                "Zone 2", Instant.now(), Instant.now(), null);
        deviceJpaRepository.save(first);
        deviceJpaRepository.save(second);

        List<Device> result = deviceRepositoryAdapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Device::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }
}
