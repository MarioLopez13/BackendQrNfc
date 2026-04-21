package com.kynsof.identity.application.command.profile.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateProfileMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "CREATE_PROFILE";

    public CreateProfileMessage(UUID id) {
        this.id = id;
    }
}
