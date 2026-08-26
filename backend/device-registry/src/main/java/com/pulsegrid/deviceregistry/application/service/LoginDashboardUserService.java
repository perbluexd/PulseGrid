package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.error.DashboardUserInactiveException;
import com.pulsegrid.deviceregistry.application.error.InvalidCredentialsException;
import com.pulsegrid.deviceregistry.application.port.in.LoginDashboardUserUseCase;
import com.pulsegrid.deviceregistry.application.port.out.AccessTokenGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.DashboardUserRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.PasswordHasherPort;
import com.pulsegrid.deviceregistry.application.port.result.LoginDashboardUserResult;
import com.pulsegrid.deviceregistry.domain.model.DashboardUser;
import com.pulsegrid.deviceregistry.domain.model.Email;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class LoginDashboardUserService implements LoginDashboardUserUseCase {
    private final DashboardUserRepositoryPort dashboardUserRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final AccessTokenGeneratorPort accessTokenGeneratorPort;

    @Override
    public LoginDashboardUserResult login(LoginDashboardUserCommand command) {
        Email email = new Email(command.email());
        DashboardUser user = dashboardUserRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasherPort.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isActive()) {
            throw new DashboardUserInactiveException(user.getId().toString());
        }

        String accessToken = accessTokenGeneratorPort.generate(user);
        long expiresIn = accessTokenGeneratorPort.getExpirationSeconds();

        return new LoginDashboardUserResult(
                accessToken,
                expiresIn,
                user.getId(),
                user.getEmail().getValue(),
                user.getRole()
        );
    }
}
