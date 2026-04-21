package com.kynsof.identity.application.command.user.adminUserChangePassword;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class AdminChangePasswordMessage implements ICommandMessage {

    private final Boolean result;
    private final String command = "CHANGE_PASSWORD";

    public AdminChangePasswordMessage(Boolean result) {
        this.result = result;
    }

}
