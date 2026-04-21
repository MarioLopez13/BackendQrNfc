package com.kynsof.identity.application.command.auth.emailLogin.verify;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class EmailLoginVerifyMessage implements ICommandMessage {

    private final TokenResponse tokenResponse;
    private final String command = "EMAIL_LOGIN_VERIFY";

    public EmailLoginVerifyMessage(TokenResponse tokenResponse) {
        this.tokenResponse = tokenResponse;
    }
}