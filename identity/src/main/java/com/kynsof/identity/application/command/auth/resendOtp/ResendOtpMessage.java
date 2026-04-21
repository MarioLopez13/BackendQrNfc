package com.kynsof.identity.application.command.auth.resendOtp;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class ResendOtpMessage implements ICommandMessage {
    private final String result;
    private final String command = "RESEND_OTP";

    public ResendOtpMessage(String result) {
        this.result = result;
    }
}
