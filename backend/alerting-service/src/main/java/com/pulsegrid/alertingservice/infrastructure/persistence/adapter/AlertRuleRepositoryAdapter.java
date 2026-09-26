package com.pulsegrid.alertingservice.infrastructure.persistence.adapter;

import com.pulsegrid.alertingservice.application.port.out.AlertRuleRepositoryPort;
import com.pulsegrid.alertingservice.domain.model.AlertRule;
import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertRuleEntity;
import com.pulsegrid.alertingservice.infrastructure.persistence.mapper.AlertRuleMapper;
import com.pulsegrid.alertingservice.infrastructure.persistence.repository.AlertRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertRuleRepositoryAdapter implements AlertRuleRepositoryPort {
    private final AlertRuleJpaRepository repository;
    private final AlertRuleMapper mapper;

    @Override
    public AlertRule save(AlertRule alertRule){
        AlertRuleEntity saved = repository.save(mapper.toEntity(alertRule));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AlertRule> findById(UUID id){
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AlertRule> findAll(){
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AlertRule> findActiveRules(UUID deviceId, Collection<UUID> groupIds, String metricType){
        List<AlertRuleEntity> entities = groupIds.isEmpty()
                ? repository.findByActiveTrueAndMetricTypeAndDeviceId(metricType, deviceId)
                : repository.findActiveForDeviceOrGroups(metricType, deviceId, groupIds);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
