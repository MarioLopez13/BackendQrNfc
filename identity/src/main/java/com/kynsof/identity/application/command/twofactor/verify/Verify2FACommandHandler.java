package com.kynsof.identity.application.command.twofactor.verify;

import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Verify2FACommandHandler implements ICommandHandler<Verify2FACommand> {

    private final ITwoFactorAuthService twoFactorAuthService;

    public Verify2FACommandHandler(ITwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void handle(Verify2FACommand command) {
        String[] backupCodes = twoFactorAuthService.verifyAndEnableTwoFactor(
                command.getUserId(),
                command.getCode()
        );

        command.setResponse(new Verify2FAMessage(
                true,
                backupCodes,
                "2FA activado exitosamente. Guarde sus codigos de respaldo en un lugar seguro."
        ));
    }
}
