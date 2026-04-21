package com.kynsof.identity.application.command.twofactor.verifylogin;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class VerifyLogin2FAMessage implements ICommandMessage {

    private final boolean valid;
    private final String message;
    private final String command = "VERIFY_LOGIN_TWO_FACTOR";

    public VerifyLogin2FAMessage(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}
