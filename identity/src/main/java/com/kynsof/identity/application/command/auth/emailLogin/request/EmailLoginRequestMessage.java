package com.kynsof.identity.application.command.auth.emailLogin.request;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class EmailLoginRequestMessage implements ICommandMessage {

    private final Boolean result;
    private final String email;
    private final String command = "EMAIL_LOGIN_REQUEST";

    public EmailLoginRequestMessage(Boolean result, String email) {
        this.result = result;
        this.email = email;
    }
}