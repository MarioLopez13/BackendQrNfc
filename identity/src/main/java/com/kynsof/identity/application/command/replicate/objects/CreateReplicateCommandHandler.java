package com.kynsof.identity.application.command.replicate.objects;

import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateReplicateCommandHandler implements ICommandHandler<CreateReplicateCommand> {

    private final IBusinessService businessService;

    public CreateReplicateCommandHandler(IBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public void handle(CreateReplicateCommand command) {

    }
}
