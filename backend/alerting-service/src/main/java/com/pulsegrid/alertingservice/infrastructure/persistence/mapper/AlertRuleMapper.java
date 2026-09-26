package com.pulsegrid.alertingservice.infrastructure.persistence.mapper;

import com.pulsegrid.alertingservice.domain.model.AlertRule;
import com.pulsegrid.alertingservice.infrastructure.persistence.entity.AlertRuleEntity;
import org.springframework.stereotype.Component;

@Component
public class AlertRuleMapper {
    public AlertRule toDomain(AlertRuleEntity entity){
        return new AlertRule(entity.getId(), entity.getDeviceId(), entity.getGroupId(), entity.getMetricType(),
                entity.getCondition(), entity.getThreshold(), entity.getSeverity(), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public AlertRuleEntity toEntity(AlertRule alertRule){
        return new AlertRuleEntity(alertRule.getId(), alertRule.getDeviceId(), alertRule.getGroupId(),
                alertRule.getMetricType(), alertRule.getCondition(), alertRule.getThreshold(),
                alertRule.getSeverity(), alertRule.isActive(), alertRule.getCreatedAt(), alertRule.getUpdatedAt());
    }
}
