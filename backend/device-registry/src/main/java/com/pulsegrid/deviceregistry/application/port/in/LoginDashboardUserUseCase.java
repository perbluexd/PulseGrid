package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.LoginDashboardUserCommand;
import com.pulsegrid.deviceregistry.application.port.result.LoginDashboardUserResult;

public interface LoginDashboardUserUseCase {
    LoginDashboardUserResult login(LoginDashboardUserCommand command);
}
