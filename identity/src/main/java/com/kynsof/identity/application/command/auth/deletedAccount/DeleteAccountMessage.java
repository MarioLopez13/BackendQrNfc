package com.kynsof.identity.application.command.auth.deletedAccount;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class DeleteAccountMessage implements ICommandMessage {

    private final Boolean result;
    private final String command = "DELETE_ACCOUNT";

    public DeleteAccountMessage(Boolean result) {
        this.result = result;
    }

}
