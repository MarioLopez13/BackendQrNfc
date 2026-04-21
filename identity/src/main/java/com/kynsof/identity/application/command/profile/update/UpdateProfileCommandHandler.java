package com.kynsof.identity.application.command.profile.update;

import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.ProfileDto;
import com.kynsof.identity.domain.interfaces.service.IPermissionService;
import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class UpdateProfileCommandHandler implements ICommandHandler<UpdateProfileCommand> {

    private final IProfileService profileService;
    private final IPermissionService permissionService;

    public UpdateProfileCommandHandler(IProfileService profileService, IPermissionService permissionService) {
        this.profileService = profileService;
        this.permissionService = permissionService;
    }

    @Override
    public void handle(UpdateProfileCommand command) {
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getId(), "Profile.id", "Profile ID cannot be null."));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getCode(), "Profile.code", "Profile code cannot be null."));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getName(), "Profile.name", "Profile name cannot be null."));

        // Verify profile exists
        profileService.findById(command.getId());

        Set<PermissionDto> permissions = new HashSet<>();
        if (command.getPermissionIds() != null && !command.getPermissionIds().isEmpty()) {
            for (UUID permissionId : command.getPermissionIds()) {
                PermissionDto permission = permissionService.findById(permissionId);
                permissions.add(permission);
            }
        }

        ProfileDto profileDto = new ProfileDto(
            command.getId(),
            command.getCode(),
            command.getName(),
            command.getDescription()
        );
        profileDto.setPermissions(permissions);

        profileService.update(profileDto);
    }
}
