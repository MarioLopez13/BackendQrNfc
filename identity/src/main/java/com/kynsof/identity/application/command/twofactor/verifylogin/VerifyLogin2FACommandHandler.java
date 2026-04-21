package com.kynsof.identity.application.command.twofactor.verifylogin;

import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.exception.BusinessException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import org.springframework.stereotype.Component;

@Component
public class VerifyLogin2FACommandHandler implements ICommandHandler<VerifyLogin2FACommand> {

    private final ITwoFactorAuthService twoFactorAuthService;

    public VerifyLogin2FACommandHandler(ITwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void handle(VerifyLogin2FACommand command) {
        boolean valid;

        if (command.isBackupCode()) {
            valid = twoFactorAuthService.verifyBackupCode(command.getUserId(), command.getCode());
        } else {
            valid = twoFactorAuthService.verifyCode(command.getUserId(), command.getCode());
        }

        if (!valid) {
            throw new BusinessException(DomainErrorMessage.INVALID_TWO_FACTOR_CODE,
                    "Codigo invalido. Intente de nuevo.");
        }

        command.setResponse(new VerifyLogin2FAMessage(
                true,
                "Verificacion exitosa."
        ));
    }
}
