package com.pulsegrid.deviceregistry.infrastructure.config;

import com.pulsegrid.deviceregistry.infrastructure.security.InternalServiceAuthenticationFilter;
import com.pulsegrid.deviceregistry.infrastructure.security.JwtAccessDeniedHandler;
import com.pulsegrid.deviceregistry.infrastructure.security.JwtAuthenticationEntryPoint;
import com.pulsegrid.deviceregistry.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/login"
    };

    private static final String[] DOCS_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] INTERNAL_ENDPOINTS = {
            "/api/v1/internal/**"
    };

    private static final String[] DEVICE_MANAGEMENT_ENDPOINTS = {
            "/api/v1/devices/**",
            "/api/v1/device-groups/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

    @Bean
    public SecurityFilterChain configureSecurity   (HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(DOCS_ENDPOINTS).permitAll()
                        .requestMatchers(INTERNAL_ENDPOINTS).hasAuthority("ROLE_INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.GET, DEVICE_MANAGEMENT_ENDPOINTS)
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VIEWER")
                        .requestMatchers(DEVICE_MANAGEMENT_ENDPOINTS).hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
