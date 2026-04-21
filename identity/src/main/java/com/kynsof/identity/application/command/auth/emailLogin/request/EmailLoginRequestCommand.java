package com.kynsof.identity.application.command.auth.emailLogin.request;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailLoginRequestCommand implements ICommand {

    private Boolean result;
    private final String email;

    public EmailLoginRequestCommand(String email) {
        this.email = email;
    }

    public static EmailLoginRequestCommand fromRequest(EmailLoginRequestRequest request) {
        return new EmailLoginRequestCommand(request.getEmail());
    }

    @Override
    public ICommandMessage getMessage() {
        return new EmailLoginRequestMessage(result, email);
    }
}