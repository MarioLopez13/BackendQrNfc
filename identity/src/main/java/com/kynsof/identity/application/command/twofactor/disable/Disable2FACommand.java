package com.kynsof.identity.application.command.twofactor.disable;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Disable2FACommand implements ICommand {

    private final UUID userId;
    private final String code;
    private Disable2FAMessage response;

    public Disable2FACommand(UUID userId, String code) {
        this.userId = userId;
        this.code = code;
    }

    public static Disable2FACommand fromRequest(Disable2FARequest request) {
        return new Disable2FACommand(request.getUserId(), request.getCode());
    }

    @Override
    public ICommandMessage getMessage() {
        return response;
    }
}
