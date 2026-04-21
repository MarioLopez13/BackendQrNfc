package com.kynsof.identity.application.command.profile.delete;

import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import org.springframework.stereotype.Component;

@Component
public class DeleteProfileCommandHandler implements ICommandHandler<DeleteProfileCommand> {

    private final IProfileService profileService;

    public DeleteProfileCommandHandler(IProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public void handle(DeleteProfileCommand command) {
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getId(), "Profile.id", "Profile ID cannot be null."));

        // Verify profile exists
        profileService.findById(command.getId());

        profileService.delete(command.getId());
    }
}
