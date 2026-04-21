package com.kynsof.identity.application.command.twofactor.setup;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class Setup2FAMessage implements ICommandMessage {

    private final String secret;
    private final String qrCodeBase64;
    private final String otpAuthUrl;
    private final String command = "SETUP_TWO_FACTOR";

    public Setup2FAMessage(String secret, String qrCodeBase64, String otpAuthUrl) {
        this.secret = secret;
        this.qrCodeBase64 = qrCodeBase64;
        this.otpAuthUrl = otpAuthUrl;
    }
}
