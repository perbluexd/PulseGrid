package com.pulsegrid.alertingservice.infrastructure.config;

import com.pulsegrid.alertingservice.infrastructure.security.JwtAccessDeniedHandler;
import com.pulsegrid.alertingservice.infrastructure.security.JwtAuthenticationEntryPoint;
import com.pulsegrid.alertingservice.infrastructure.security.JwtAuthenticationFilter;
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

    private static final String[] DOCS_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/actuator/health/**"
    };

    private static final String[] ACTUATOR_RESTRICTED_ENDPOINTS = {
            "/actuator/**"
    };

    private static final String[] ALERTING_ENDPOINTS = {
            "/api/v1/alert-rules/**",
            "/api/v1/alerts/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain configureSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(DOCS_ENDPOINTS).permitAll()
                        .requestMatchers(ACTUATOR_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ACTUATOR_RESTRICTED_ENDPOINTS).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, ALERTING_ENDPOINTS).hasAnyAuthority("ROLE_ADMIN", "ROLE_VIEWER")
                        .requestMatchers(ALERTING_ENDPOINTS).hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
