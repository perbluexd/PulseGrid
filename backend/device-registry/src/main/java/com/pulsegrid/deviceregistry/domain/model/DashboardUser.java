package com.pulsegrid.deviceregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DashboardUser {
    @EqualsAndHashCode.Include
    private final UUID id;
    private final Email email;
    private String passwordHash;
    private final Role role;
    private boolean isActive;
    private final Instant createdAt;
    private Instant updatedAt;

    public DashboardUser(UUID id, Email email, String passwordHash, Role role,
                          boolean isActive, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void activate(){
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
    public void deactivate(){
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    public void changePassword(String newPasswordHash){
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }
}
