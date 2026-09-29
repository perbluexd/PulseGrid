package com.pulsegrid.alertingservice.infrastructure.persistence.adapter;

import com.pulsegrid.alertingservice.application.port.out.AlertRepositoryPort;
import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.domain.model.AlertSeverity;
import com.pulsegrid.alertingservice.domain.model.AlertStatus;
import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertEntity;
import com.pulsegrid.alertingservice.infrastructure.persistence.mapper.AlertMapper;
import com.pulsegrid.alertingservice.infrastructure.persistence.repository.AlertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertRepositoryAdapter implements AlertRepositoryPort {

    private static final List<AlertStatus> OPEN_STATUSES = List.of(AlertStatus.TRIGGERED, AlertStatus.ACKNOWLEDGED);

    private final AlertJpaRepository repository;
    private final AlertMapper mapper;

    @Override
    public Alert save(Alert alert){
        AlertEntity saved = repository.save(mapper.toEntity(alert));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Alert> findById(UUID id){
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Alert> findAll(AlertStatus status, AlertSeverity severity, UUID deviceId){
        Specification<AlertEntity> spec = Specification.allOf(
                equalsIfPresent("status", status),
                equalsIfPresent("severity", severity),
                equalsIfPresent("deviceId", deviceId)
        );
        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "triggeredAt")).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsOpenAlert(UUID alertRuleId, UUID deviceId){
        return repository.existsByAlertRuleIdAndDeviceIdAndStatusIn(alertRuleId, deviceId, OPEN_STATUSES);
    }

    private Specification<AlertEntity> equalsIfPresent(String field, Object value){
        return (root, query, cb) -> value == null ? null : cb.equal(root.get(field), value);
    }
}
