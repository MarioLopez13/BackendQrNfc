package com.kynsof.identity.application.command.user.update.steptwo;

import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserSystemStepTowCommandHandler implements ICommandHandler<UpdateUserSystemStepTwoCommand> {

    private final IUserSystemService systemService;


    public UpdateUserSystemStepTowCommandHandler(IUserSystemService allergyService) {
        this.systemService = allergyService;
    }

    @Override
    public void handle(UpdateUserSystemStepTwoCommand command) {
        UserSystemDto update = this.systemService.findById(command.getId());
        update.setImage(command.getImage());
        update.setUserType(command.getUserType());
        systemService.update(update);
    }
}
