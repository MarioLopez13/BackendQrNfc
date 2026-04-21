package com.kynsof.identity.application.command.twofactor.verifylogin;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VerifyLogin2FACommand implements ICommand {

    private final UUID userId;
    private final String code;
    private final boolean isBackupCode;
    private VerifyLogin2FAMessage response;

    public VerifyLogin2FACommand(UUID userId, String code, boolean isBackupCode) {
        this.userId = userId;
        this.code = code;
        this.isBackupCode = isBackupCode;
    }

    public static VerifyLogin2FACommand fromRequest(VerifyLogin2FARequest request) {
        return new VerifyLogin2FACommand(
                request.getUserId(),
                request.getCode(),
                request.isBackupCode()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return response;
    }
}
