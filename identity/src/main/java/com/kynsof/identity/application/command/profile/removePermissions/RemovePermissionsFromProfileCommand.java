package com.kynsof.identity.application.command.profile.removePermissions;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RemovePermissionsFromProfileCommand implements ICommand {

    private final UUID profileId;
    private final List<UUID> permissionIds;

    public static RemovePermissionsFromProfileCommand fromRequest(RemovePermissionsFromProfileRequest request) {
        return new RemovePermissionsFromProfileCommand(
            request.getProfileId(),
            request.getPermissionIds()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new RemovePermissionsFromProfileMessage(profileId);
    }
}
