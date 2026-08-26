package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;

import java.util.Optional;

public interface DashboardUserRepositoryPort {
    Optional<DashboardUser> findByEmail(Email email);
}
