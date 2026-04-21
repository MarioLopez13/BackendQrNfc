package com.kynsof.identity.application.command.auth.registerGoogle;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegisterGoogleUserMessage implements ICommandMessage {
    private UUID userId;
    private String email;
    private String name;
    private String lastName;
    private boolean isNewUser;
    private final String command = "REGISTER_GOOGLE_USER";

    public RegisterGoogleUserMessage() {
    }

    public RegisterGoogleUserMessage(UUID userId, String email, String name, String lastName, boolean isNewUser) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.isNewUser = isNewUser;
    }
}
