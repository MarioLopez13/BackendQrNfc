package com.kynsof.identity.application.command.twofactor.disable;

import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Disable2FACommandHandler implements ICommandHandler<Disable2FACommand> {

    private final ITwoFactorAuthService twoFactorAuthService;

    public Disable2FACommandHandler(ITwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void handle(Disable2FACommand command) {
        twoFactorAuthService.disableTwoFactor(command.getUserId(), command.getCode());

        command.setResponse(new Disable2FAMessage(
                true,
                "2FA desactivado exitosamente."
        ));
    }
}
