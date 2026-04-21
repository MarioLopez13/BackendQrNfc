package com.kynsof.identity.application.command.session.deleteAllSessions;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteAllSessionsCommand implements ICommand {
    private String userId;

    @Override
    public ICommandMessage getMessage() {
        return new DeleteAllSessionsMessage(userId);
    }
}
