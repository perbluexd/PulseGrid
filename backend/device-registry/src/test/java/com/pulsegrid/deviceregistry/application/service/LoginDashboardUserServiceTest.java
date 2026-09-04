package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.error.DashboardUserInactiveException;
import com.pulsegrid.deviceregistry.application.error.InvalidCredentialsException;
import com.pulsegrid.deviceregistry.application.port.out.AccessTokenGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.DashboardUserRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.PasswordHasherPort;
import com.pulsegrid.deviceregistry.application.port.result.LoginDashboardUserResult;
import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;
import com.pulsegrid.deviceregistry.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginDashboardUserServiceTest {

    @Mock
    private DashboardUserRepositoryPort dashboardUserRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private AccessTokenGeneratorPort accessTokenGeneratorPort;

    @InjectMocks
    private LoginDashboardUserService loginDashboardUserService;

    @Test
    void shouldLoginWhenCredentialsAreValidAndUserIsActive() {
        DashboardUser user = new DashboardUser(UUID.randomUUID(), new Email("percy@gmail.com"), "hashed-pw",
                Role.ADMIN, true, Instant.now(), Instant.now());

        when(dashboardUserRepositoryPort.findByEmail(new Email("percy@gmail.com"))).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("raw-password", "hashed-pw")).thenReturn(true);
        when(accessTokenGeneratorPort.generate(user)).thenReturn("jwt-token");
        when(accessTokenGeneratorPort.getExpirationSeconds()).thenReturn(1800L);

        LoginDashboardUserResult result = loginDashboardUserService.login(
                new LoginDashboardUserCommand("percy@gmail.com", "raw-password"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresIn()).isEqualTo(1800L);
        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(dashboardUserRepositoryPort.findByEmail(new Email("percy@gmail.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginDashboardUserService.login(
                new LoginDashboardUserCommand("percy@gmail.com", "raw-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(accessTokenGeneratorPort, never()).generate(any());
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        DashboardUser user = new DashboardUser(UUID.randomUUID(), new Email("percy@gmail.com"), "hashed-pw",
                Role.ADMIN, true, Instant.now(), Instant.now());

        when(dashboardUserRepositoryPort.findByEmail(new Email("percy@gmail.com"))).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong-password", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> loginDashboardUserService.login(
                new LoginDashboardUserCommand("percy@gmail.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(accessTokenGeneratorPort, never()).generate(any());
    }

    @Test
    void shouldThrowWhenUserIsInactive() {
        DashboardUser user = new DashboardUser(UUID.randomUUID(), new Email("percy@gmail.com"), "hashed-pw",
                Role.ADMIN, false, Instant.now(), Instant.now());

        when(dashboardUserRepositoryPort.findByEmail(new Email("percy@gmail.com"))).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("raw-password", "hashed-pw")).thenReturn(true);

        assertThatThrownBy(() -> loginDashboardUserService.login(
                new LoginDashboardUserCommand("percy@gmail.com", "raw-password")))
                .isInstanceOf(DashboardUserInactiveException.class);

        verify(accessTokenGeneratorPort, never()).generate(any());
    }
}
