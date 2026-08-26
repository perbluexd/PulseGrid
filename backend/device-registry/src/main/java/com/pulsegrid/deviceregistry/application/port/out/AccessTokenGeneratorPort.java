package com.pulsegrid.deviceregistry.application.port.out;

import com.pulsegrid.deviceregistry.domain.model.DashboardUser;

public interface AccessTokenGeneratorPort {
    String generate(DashboardUser dashboardUser);
    long getExpirationSeconds();
}
