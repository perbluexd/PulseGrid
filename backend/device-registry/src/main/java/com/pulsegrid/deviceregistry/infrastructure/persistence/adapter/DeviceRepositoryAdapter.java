package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {
    private final DeviceJpaRepository repository;
    private final DeviceMapper mapper;

    @Override
    public Device save(Device device){
        DeviceEntity saved = repository.save(mapper.toEntity(device));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Device> findById(UUID id){
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Device> findAll(){
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}
