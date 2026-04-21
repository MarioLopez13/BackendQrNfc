package com.kynsof.identity.application.command.lead.updatestatus;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateLeadStatusMessage implements ICommandMessage {

    private UUID id;
    private String command = "UPDATE_LEAD_STATUS";

    public UpdateLeadStatusMessage(UUID id) {
        this.id = id;
    }
}
