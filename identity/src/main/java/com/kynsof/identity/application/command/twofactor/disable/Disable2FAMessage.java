package com.kynsof.identity.application.command.twofactor.disable;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class Disable2FAMessage implements ICommandMessage {

    private final boolean success;
    private final String message;
    private final String command = "DISABLE_TWO_FACTOR";

    public Disable2FAMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
