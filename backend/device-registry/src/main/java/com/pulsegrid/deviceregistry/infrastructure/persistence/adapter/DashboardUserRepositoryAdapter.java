package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.application.port.out.DashboardUserRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DashboardUserMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DashboardUserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DashboardUserRepositoryAdapter implements DashboardUserRepositoryPort {
    private final DashboardUserJpaRepository repository;
    private final DashboardUserMapper mapper;

    @Override
    public Optional<DashboardUser> findByEmail(Email email){
        return repository.findByEmail(email.getValue()).map(mapper::toDomain);
    }
}
