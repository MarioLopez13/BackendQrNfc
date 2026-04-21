package com.kynsof.identity.application.command.profile.removePermissions;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RemovePermissionsFromProfileMessage implements ICommandMessage {

    private final UUID profileId;

    public static RemovePermissionsFromProfileMessage fromCommand(RemovePermissionsFromProfileCommand command) {
        return new RemovePermissionsFromProfileMessage(command.getProfileId());
    }
}
