package com.kynsof.identity.application.command.lead.updatestatus;

import com.kynsof.identity.domain.interfaces.service.ILeadService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateLeadStatusCommandHandler implements ICommandHandler<UpdateLeadStatusCommand> {

    private final ILeadService leadService;

    public UpdateLeadStatusCommandHandler(ILeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public void handle(UpdateLeadStatusCommand command) {
        leadService.updateStatus(command.getId(), command.getStatus(), command.getNotes());
    }
}
