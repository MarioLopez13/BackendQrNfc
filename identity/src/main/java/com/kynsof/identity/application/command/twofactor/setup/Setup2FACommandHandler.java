package com.kynsof.identity.application.command.twofactor.setup;

import com.kynsof.identity.domain.dto.TwoFactorSetupDto;
import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Setup2FACommandHandler implements ICommandHandler<Setup2FACommand> {

    private final ITwoFactorAuthService twoFactorAuthService;

    public Setup2FACommandHandler(ITwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void handle(Setup2FACommand command) {
        TwoFactorSetupDto setup = twoFactorAuthService.setupTwoFactor(
                command.getUserId(),
                command.getEmail()
        );

        command.setResponse(new Setup2FAMessage(
                setup.getSecret(),
                setup.getQrCodeBase64(),
                setup.getOtpAuthUrl()
        ));
    }
}
