package com.kynsof.identity.application.command.twofactor.verify;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Verify2FACommand implements ICommand {

    private final UUID userId;
    private final String code;
    private Verify2FAMessage response;

    public Verify2FACommand(UUID userId, String code) {
        this.userId = userId;
        this.code = code;
    }

    public static Verify2FACommand fromRequest(Verify2FARequest request) {
        return new Verify2FACommand(request.getUserId(), request.getCode());
    }

    @Override
    public ICommandMessage getMessage() {
        return response;
    }
}
