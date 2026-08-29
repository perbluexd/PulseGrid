package com.pulsegrid.deviceregistry.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "device_group_memberships")
public class DeviceGroupMembershipEntity {

    @Id
    @Column(nullable = false)
    private UUID id;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
