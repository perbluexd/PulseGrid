package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceGroupMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceGroupJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DeviceGroupRepositoryAdapter.class, DeviceGroupMapper.class})
@Testcontainers
class DeviceGroupRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DeviceGroupRepositoryAdapter deviceGroupRepositoryAdapter;

    @Autowired
    private DeviceGroupJpaRepository deviceGroupJpaRepository;

    @Test
    void shouldSaveDeviceGroupAndPersistItInDatabase() {
        DeviceGroup deviceGroup = new DeviceGroup(UUID.randomUUID(), "Warehouse Sensors", "All warehouse sensors");

        DeviceGroup result = deviceGroupRepositoryAdapter.save(deviceGroup);
        deviceGroupJpaRepository.flush();

        Optional<DeviceGroupEntity> persisted = deviceGroupJpaRepository.findById(result.getId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getName()).isEqualTo("Warehouse Sensors");
        assertThat(persisted.get().getDescription()).isEqualTo("All warehouse sensors");
    }

    @Test
    void shouldFindDeviceGroupByIdWhenItExists() {
        UUID id = UUID.randomUUID();
        DeviceGroupEntity entity = new DeviceGroupEntity(id, "Zone A", "Devices in zone A");
        deviceGroupJpaRepository.save(entity);

        Optional<DeviceGroup> result = deviceGroupRepositoryAdapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Zone A");
        assertThat(result.get().getDescription()).isEqualTo("Devices in zone A");
    }

    @Test
    void shouldReturnEmptyWhenDeviceGroupIdDoesNotExist() {
        Optional<DeviceGroup> result = deviceGroupRepositoryAdapter.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllDeviceGroups() {
        DeviceGroupEntity first = new DeviceGroupEntity(UUID.randomUUID(), "Group 1", null);
        DeviceGroupEntity second = new DeviceGroupEntity(UUID.randomUUID(), "Group 2", null);
        deviceGroupJpaRepository.save(first);
        deviceGroupJpaRepository.save(second);

        List<DeviceGroup> result = deviceGroupRepositoryAdapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DeviceGroup::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldDeleteDeviceGroupById() {
        UUID id = UUID.randomUUID();
        DeviceGroupEntity entity = new DeviceGroupEntity(id, "Temporary Group", null);
        deviceGroupJpaRepository.save(entity);

        deviceGroupRepositoryAdapter.deleteById(id);
        deviceGroupJpaRepository.flush();

        assertThat(deviceGroupJpaRepository.findById(id)).isEmpty();
    }
}
