package com.pulsegrid.deviceregistry.infrastructure.persistence.mapper;

import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;
import com.pulsegrid.deviceregistry.infrastructure.persistence.entity.DashboardUserEntity;
import org.springframework.stereotype.Component;

@Component
public class DashboardUserMapper {
    public DashboardUser toDomain(DashboardUserEntity entity){
        Email email = new Email(entity.getEmail());
        return new DashboardUser(entity.getId(), email, entity.getPasswordHash(), entity.getRole(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public DashboardUserEntity toEntity(DashboardUser dashboardUser){
        return new DashboardUserEntity(dashboardUser.getId(), dashboardUser.getEmail().getValue(),
                dashboardUser.getPasswordHash(), dashboardUser.getRole(), dashboardUser.isActive(),
                dashboardUser.getCreatedAt(), dashboardUser.getUpdatedAt());
    }
}
