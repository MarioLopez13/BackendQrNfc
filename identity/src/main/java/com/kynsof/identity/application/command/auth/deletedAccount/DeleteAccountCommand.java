package com.kynsof.identity.application.command.auth.deletedAccount;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountCommand implements ICommand {
    private Boolean resul;
    private String email;
    private String password;

    public DeleteAccountCommand(String email, String password) {

        this.email = email;
        this.password = password;
    }



    @Override
    public ICommandMessage getMessage() {
        return new DeleteAccountMessage(resul);
    }
}
