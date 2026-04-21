package com.kynsof.identity.application.command.profile.removePermissions;

import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RemovePermissionsFromProfileCommandHandler implements ICommandHandler<RemovePermissionsFromProfileCommand> {

    private final IProfileService profileService;

    public RemovePermissionsFromProfileCommandHandler(IProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public void handle(RemovePermissionsFromProfileCommand command) {
        profileService.removePermissionsFromProfile(command.getProfileId(), command.getPermissionIds());
    }
}
