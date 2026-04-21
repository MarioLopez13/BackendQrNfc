package com.kynsof.identity.application.query.twofactor.status;

import com.kynsof.identity.domain.dto.TwoFactorAuthDto;
import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Get2FAStatusQueryHandler implements IQueryHandler<Get2FAStatusQuery, Get2FAStatusResponse> {

    private final ITwoFactorAuthService twoFactorAuthService;

    public Get2FAStatusQueryHandler(ITwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public Get2FAStatusResponse handle(Get2FAStatusQuery query) {
        Optional<TwoFactorAuthDto> twoFactorOpt = twoFactorAuthService.getTwoFactorStatus(query.getUserId());

        if (twoFactorOpt.isEmpty()) {
            return new Get2FAStatusResponse(false, false, null, 0);
        }

        TwoFactorAuthDto twoFactor = twoFactorOpt.get();
        int remainingBackupCodes = 0;

        if (twoFactor.getBackupCodes() != null && !twoFactor.getBackupCodes().isEmpty()) {
            remainingBackupCodes = twoFactor.getBackupCodes().split(",").length;
        }

        return new Get2FAStatusResponse(
                twoFactor.isEnabled(),
                true,
                twoFactor.getEnabledAt(),
                remainingBackupCodes
        );
    }
}
