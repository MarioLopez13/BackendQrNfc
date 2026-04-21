package com.kynsof.identity.application.command.twofactor.setup;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Setup2FACommand implements ICommand {

    private final UUID userId;
    private final String email;
    private Setup2FAMessage response;

    public Setup2FACommand(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public static Setup2FACommand fromRequest(Setup2FARequest request) {
        return new Setup2FACommand(request.getUserId(), request.getEmail());
    }

    @Override
    public ICommandMessage getMessage() {
        return response;
    }
}
