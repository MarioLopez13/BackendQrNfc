package com.kynsof.identity.application.command.profile.update;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateProfileMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "UPDATE_PROFILE";

    public UpdateProfileMessage(UUID id) {
        this.id = id;
    }
}
