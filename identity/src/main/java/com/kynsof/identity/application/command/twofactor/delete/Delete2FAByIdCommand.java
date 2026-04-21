package com.kynsof.identity.application.command.twofactor.delete;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Delete2FAByIdCommand implements ICommand {

    private UUID id;
    private ICommandMessage message;

    public Delete2FAByIdCommand(UUID id) {
        this.id = id;
    }

    @Override
    public ICommandMessage getMessage() {
        return message;
    }

    public void setMessage(Delete2FAByIdMessage message) {
        this.message = message;
    }
}
