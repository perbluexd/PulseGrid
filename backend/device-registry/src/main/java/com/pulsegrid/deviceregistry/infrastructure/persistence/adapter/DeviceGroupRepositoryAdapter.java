package com.pulsegrid.deviceregistry.infrastructure.persistence.adapter;

import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupRepositoryPort;
import com.pulsegrid.deviceregistry.domain.model.DeviceGroup;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DeviceGroupEntity;
import com.pulsegrid.deviceregistry.infrastructure.persistence.mapper.DeviceGroupMapper;
import com.pulsegrid.deviceregistry.infrastructure.persistence.repository.DeviceGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceGroupRepositoryAdapter implements DeviceGroupRepositoryPort {
    private final DeviceGroupJpaRepository repository;
    private final DeviceGroupMapper mapper;

    @Override
    public DeviceGroup save(DeviceGroup deviceGroup){
        DeviceGroupEntity saved = repository.save(mapper.toEntity(deviceGroup));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DeviceGroup> findById(UUID id){
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<DeviceGroup> findAll(){
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id){
        repository.deleteById(id);
    }
}
