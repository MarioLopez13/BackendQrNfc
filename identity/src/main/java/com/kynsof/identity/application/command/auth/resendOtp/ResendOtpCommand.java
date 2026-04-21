package com.kynsof.identity.application.command.auth.resendOtp;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpCommand implements ICommand {
    private final String email;
    private ICommandMessage message;

    public ResendOtpCommand(String email) {
        this.email = email;
    }

    public static ResendOtpCommand fromRequest(ResendOtpRequest request) {
        return new ResendOtpCommand(request.getEmail());
    }

    @Override
    public ICommandMessage getMessage() {
        return this.message;
    }
}
