package com.pulsegrid.aggregationservice.infrastructure.config;

import com.pulsegrid.aggregationservice.infrastructure.security.InternalServiceAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ACTUATOR_ENDPOINTS = {
            "/actuator/**"
    };

    private static final String[] INTERNAL_ENDPOINTS = {
            "/api/v1/internal/**"
    };

    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

    @Bean
    public SecurityFilterChain configureSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
                        .requestMatchers(INTERNAL_ENDPOINTS).hasAuthority("ROLE_INTERNAL_SERVICE")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(internalServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
