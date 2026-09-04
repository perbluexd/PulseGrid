package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RemoveDeviceFromGroupCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotInGroupException;
import com.pulsegrid.deviceregistry.application.port.out.DeviceGroupMembershipRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveDeviceFromGroupServiceTest {

    @Mock
    private DeviceGroupMembershipRepositoryPort deviceGroupMembershipRepositoryPort;

    @InjectMocks
    private RemoveDeviceFromGroupService removeDeviceFromGroupService;

    @Test
    void shouldRemoveDeviceFromGroupWhenMembershipExists() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(deviceId, groupId)).thenReturn(true);

        removeDeviceFromGroupService.remove(new RemoveDeviceFromGroupCommand(deviceId, groupId));

        verify(deviceGroupMembershipRepositoryPort).deleteByDeviceIdAndGroupId(deviceId, groupId);
    }

    @Test
    void shouldThrowWhenMembershipDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(deviceGroupMembershipRepositoryPort.existsByDeviceIdAndGroupId(deviceId, groupId)).thenReturn(false);

        assertThatThrownBy(() -> removeDeviceFromGroupService.remove(new RemoveDeviceFromGroupCommand(deviceId, groupId)))
                .isInstanceOf(DeviceNotInGroupException.class);

        verify(deviceGroupMembershipRepositoryPort, never()).deleteByDeviceIdAndGroupId(any(), any());
    }
}
