package com.pulsegrid.queryapi.infrastructure.config;

import com.pulsegrid.queryapi.infrastructure.security.JsonAuthenticationEntryPoint;
import com.pulsegrid.queryapi.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] GRAPHIQL_ENDPOINTS = {
            "/graphiql",
            "/graphiql/**"
    };

    private static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/actuator/health/**"
    };

    private static final String[] ACTUATOR_RESTRICTED_ENDPOINTS = {
            "/actuator/**"
    };

    private static final String[] GRAPHQL_ENDPOINTS = {
            "/graphql",
            "/graphql/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain configureSecurity(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(GRAPHIQL_ENDPOINTS).permitAll()
                        .requestMatchers(ACTUATOR_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ACTUATOR_RESTRICTED_ENDPOINTS).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(GRAPHQL_ENDPOINTS).hasAnyAuthority("ROLE_ADMIN", "ROLE_VIEWER")
                        .anyRequest().denyAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jsonAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
