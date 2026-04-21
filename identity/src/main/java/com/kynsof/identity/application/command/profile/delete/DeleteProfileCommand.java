package com.kynsof.identity.application.command.profile.delete;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DeleteProfileCommand implements ICommand {

    private final UUID id;

    public DeleteProfileCommand(UUID id) {
        this.id = id;
    }

    @Override
    public ICommandMessage getMessage() {
        return new DeleteProfileMessage(id);
    }
}
