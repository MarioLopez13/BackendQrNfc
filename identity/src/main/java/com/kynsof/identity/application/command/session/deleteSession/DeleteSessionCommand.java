package com.kynsof.identity.application.command.session.deleteSession;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteSessionCommand implements ICommand {
    private String userId;
    private String sessionId;

    @Override
    public ICommandMessage getMessage() {
        return new DeleteSessionMessage(userId, sessionId);
    }
}
