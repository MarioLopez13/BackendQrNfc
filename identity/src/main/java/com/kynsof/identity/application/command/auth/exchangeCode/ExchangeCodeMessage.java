package com.kynsof.identity.application.command.auth.exchangeCode;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class ExchangeCodeMessage implements ICommandMessage {
    private final TokenResponse tokenResponse;
    private final String command = "EXCHANGE_CODE";

    public ExchangeCodeMessage(TokenResponse tokenResponse) {
        this.tokenResponse = tokenResponse;
    }
}