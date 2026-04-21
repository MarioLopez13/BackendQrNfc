package com.kynsof.identity.application.command.profile.delete;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DeleteProfileMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "DELETE_PROFILE";

    public DeleteProfileMessage(UUID id) {
        this.id = id;
    }
}
