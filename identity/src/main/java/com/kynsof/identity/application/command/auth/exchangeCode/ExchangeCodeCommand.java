package com.kynsof.identity.application.command.auth.exchangeCode;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExchangeCodeCommand implements ICommand {
    private String code;
    private String redirectUri;
    private TokenResponse tokenResponse;

    public ExchangeCodeCommand(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    @Override
    public ICommandMessage getMessage() {
        return new ExchangeCodeMessage(tokenResponse);
    }
}