package com.pulsegrid.deviceregistry.infrastructure.security;

import com.pulsegrid.deviceregistry.application.port.out.AccessTokenGeneratorPort;
import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class AccessTokenGeneratorAdapter implements AccessTokenGeneratorPort {
    private final JwtKeyProvider jwtKeyProvider;
    private final long accessTokenExpirationMinutes;

    public AccessTokenGeneratorAdapter(JwtKeyProvider jwtKeyProvider,
                                        @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes){
        this.jwtKeyProvider = jwtKeyProvider;
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    @Override
    public String generate(DashboardUser dashboardUser){
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(dashboardUser.getId().toString())
                .claim("email", dashboardUser.getEmail().getValue())
                .claim("role", dashboardUser.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtKeyProvider.getKey())
                .compact();
    }

    @Override
    public long getExpirationSeconds(){
        return accessTokenExpirationMinutes * 60;
    }
}
