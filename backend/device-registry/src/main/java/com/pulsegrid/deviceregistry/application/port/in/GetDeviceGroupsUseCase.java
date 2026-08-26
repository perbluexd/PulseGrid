package com.pulsegrid.deviceregistry.application.port.in;

import com.pulsegrid.deviceregistry.application.command.GetDeviceGroupsCommand;
import com.pulsegrid.deviceregistry.application.port.result.GetDeviceGroupsResult;

public interface GetDeviceGroupsUseCase {
    GetDeviceGroupsResult getGroups(GetDeviceGroupsCommand command);
}
