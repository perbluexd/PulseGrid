package com.pulsegrid.ingestiongateway.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalServiceTokenProvider {
    private final String token;

    public InternalServiceTokenProvider(@Value("${app.internal.service-token}") String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
