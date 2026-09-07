package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.domain.model.DeviceGroupMembership;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupMembershipEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceGroupMembershipMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceGroupJpaRepository;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceGroupMembershipJpaRepository;
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
@Import({DeviceGroupMembershipRepositoryAdapter.class, DeviceGroupMembershipMapper.class})
@Testcontainers
class DeviceGroupMembershipRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DeviceGroupMembershipRepositoryAdapter deviceGroupMembershipRepositoryAdapter;

    @Autowired
    private DeviceGroupMembershipJpaRepository deviceGroupMembershipJpaRepository;

    @Autowired
    private DeviceJpaRepository deviceJpaRepository;

    @Autowired
    private DeviceGroupJpaRepository deviceGroupJpaRepository;

    private UUID seedDevice() {
        UUID deviceId = UUID.randomUUID();
        deviceJpaRepository.save(new DeviceEntity(deviceId, "Device", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Zone", Instant.now(), Instant.now(), null));
        return deviceId;
    }

    private UUID seedGroup() {
        UUID groupId = UUID.randomUUID();
        deviceGroupJpaRepository.save(new DeviceGroupEntity(groupId, "Group", null));
        return groupId;
    }

    @Test
    void shouldSaveMembershipAndPersistItInDatabase() {
        DeviceGroupMembership membership = new DeviceGroupMembership(UUID.randomUUID(), seedDevice(),
                seedGroup(), Instant.now());

        DeviceGroupMembership result = deviceGroupMembershipRepositoryAdapter.save(membership);
        deviceGroupMembershipJpaRepository.flush();

        Optional<DeviceGroupMembershipEntity> persisted = deviceGroupMembershipJpaRepository.findById(result.getId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getDeviceId()).isEqualTo(membership.getDeviceId());
        assertThat(persisted.get().getGroupId()).isEqualTo(membership.getGroupId());
    }

    @Test
    void shouldReturnTrueWhenMembershipExists() {
        UUID deviceId = seedDevice();
        UUID groupId = seedGroup();
        deviceGroupMembershipJpaRepository.save(
                new DeviceGroupMembershipEntity(UUID.randomUUID(), deviceId, groupId, Instant.now()));

        boolean result = deviceGroupMembershipRepositoryAdapter.existsByDeviceIdAndGroupId(deviceId, groupId);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMembershipDoesNotExist() {
        boolean result = deviceGroupMembershipRepositoryAdapter
                .existsByDeviceIdAndGroupId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isFalse();
    }

    @Test
    void shouldFindGroupIdsByDeviceId() {
        UUID deviceId = seedDevice();
        UUID firstGroupId = seedGroup();
        UUID secondGroupId = seedGroup();
        deviceGroupMembershipJpaRepository.save(
                new DeviceGroupMembershipEntity(UUID.randomUUID(), deviceId, firstGroupId, Instant.now()));
        deviceGroupMembershipJpaRepository.save(
                new DeviceGroupMembershipEntity(UUID.randomUUID(), deviceId, secondGroupId, Instant.now()));

        List<UUID> result = deviceGroupMembershipRepositoryAdapter.findGroupIdsByDeviceId(deviceId);

        assertThat(result).containsExactlyInAnyOrder(firstGroupId, secondGroupId);
    }

    @Test
    void shouldDeleteMembershipByDeviceIdAndGroupId() {
        UUID deviceId = seedDevice();
        UUID groupId = seedGroup();
        deviceGroupMembershipJpaRepository.save(
                new DeviceGroupMembershipEntity(UUID.randomUUID(), deviceId, groupId, Instant.now()));

        deviceGroupMembershipRepositoryAdapter.deleteByDeviceIdAndGroupId(deviceId, groupId);

        assertThat(deviceGroupMembershipRepositoryAdapter.existsByDeviceIdAndGroupId(deviceId, groupId)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenGroupHasAnyMembership() {
        UUID groupId = seedGroup();
        deviceGroupMembershipJpaRepository.save(
                new DeviceGroupMembershipEntity(UUID.randomUUID(), seedDevice(), groupId, Instant.now()));

        boolean result = deviceGroupMembershipRepositoryAdapter.existsByGroupId(groupId);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenGroupHasNoMembership() {
        boolean result = deviceGroupMembershipRepositoryAdapter.existsByGroupId(UUID.randomUUID());

        assertThat(result).isFalse();
    }
}
