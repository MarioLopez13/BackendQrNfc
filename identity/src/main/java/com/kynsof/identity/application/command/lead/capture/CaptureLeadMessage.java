package com.kynsof.identity.application.command.lead.capture;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CaptureLeadMessage implements ICommandMessage {

    private UUID id;
    private String command = "CAPTURE_LEAD";

    public CaptureLeadMessage(UUID id) {
        this.id = id;
    }
}
