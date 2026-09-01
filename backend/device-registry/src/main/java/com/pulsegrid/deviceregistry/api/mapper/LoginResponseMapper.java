package com.pulsegrid.deviceregistry.api.mapper;

import com.pulsegrid.deviceregistry.api.dto.auth.LoginResponse;
import com.pulsegrid.deviceregistry.application.port.result.LoginDashboardUserResult;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

    public LoginResponse toResponse(LoginDashboardUserResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.expiresIn(),
                result.userId(),
                result.email(),
                result.role().name()
        );
    }
}
