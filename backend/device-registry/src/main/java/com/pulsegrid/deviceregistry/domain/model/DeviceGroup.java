package com.pulsegrid.deviceregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceGroup {
    @EqualsAndHashCode.Include
    private final UUID id;
    private String name;
    private String description;

    public DeviceGroup(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void updateDetails(String name, String description){
        this.name = name;
        this.description = description;
    }
}
