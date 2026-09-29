package com.pulsegrid.alertingservice.infrastructure.persistence.mapper;

import com.pulsegrid.alertingservice.domain.model.Alert;
import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertEntity;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {
    public Alert toDomain(AlertEntity entity){
        return new Alert(entity.getId(), entity.getAlertRuleId(), entity.getDeviceId(), entity.getTriggeredValue(),
                entity.getSeverity(), entity.getStatus(), entity.getTriggeredAt(), entity.getResolvedAt(),
                entity.getAcknowledgedBy(), entity.getResolvedBy());
    }

    public AlertEntity toEntity(Alert alert){
        return new AlertEntity(alert.getId(), alert.getAlertRuleId(), alert.getDeviceId(), alert.getTriggeredValue(),
                alert.getSeverity(), alert.getStatus(), alert.getTriggeredAt(), alert.getResolvedAt(),
                alert.getAcknowledgedBy(), alert.getResolvedBy());
    }
}
