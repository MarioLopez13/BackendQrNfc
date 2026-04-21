package com.kynsof.identity.application.command.lead.updatestatus;

import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateLeadStatusCommand implements ICommand {

    private UUID id;
    private ELeadStatus status;
    private String notes;

    public UpdateLeadStatusCommand(UUID id, UpdateLeadStatusRequest request) {
        this.id = id;
        this.status = request.getStatus();
        this.notes = request.getNotes();
    }

    @Override
    public ICommandMessage getMessage() {
        return new UpdateLeadStatusMessage(id);
    }
}
