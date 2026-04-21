package com.kynsof.identity.application.command.auth.registerGoogle;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterGoogleUserCommand implements ICommand {
    private String code;
    private String redirectUri;

    @Override
    public ICommandMessage getMessage() {
        return new RegisterGoogleUserMessage();
    }
}
