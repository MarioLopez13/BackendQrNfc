package com.kynsof.identity.application.command.auth.exchangeCode;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.infrastructure.services.AuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ExchangeCodeCommandHandler implements ICommandHandler<ExchangeCodeCommand> {
    private final IAuthService authService;

    public ExchangeCodeCommandHandler(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(ExchangeCodeCommand command) {
        TokenResponse tokenResponse = authService.exchangeCodeForToken(command.getCode(), command.getRedirectUri());
        command.setTokenResponse(tokenResponse);
    }
}
