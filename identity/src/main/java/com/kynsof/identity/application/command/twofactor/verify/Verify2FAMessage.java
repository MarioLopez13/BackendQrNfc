package com.kynsof.identity.application.command.twofactor.verify;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class Verify2FAMessage implements ICommandMessage {

    private final boolean success;
    private final String[] backupCodes;
    private final String message;
    private final String command = "VERIFY_TWO_FACTOR";

    public Verify2FAMessage(boolean success, String[] backupCodes, String message) {
        this.success = success;
        this.backupCodes = backupCodes;
        this.message = message;
    }
}
