package com.kynsof.identity.application.command.auth.emailLogin.verify;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailLoginVerifyCommand implements ICommand {

    private TokenResponse tokenResponse;
    private final String email;
    private final String code;

    public EmailLoginVerifyCommand(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public static EmailLoginVerifyCommand fromRequest(EmailLoginVerifyRequest request) {
        return new EmailLoginVerifyCommand(request.getEmail(), request.getCode());
    }

    @Override
    public ICommandMessage getMessage() {
        return new EmailLoginVerifyMessage(tokenResponse);
    }
}