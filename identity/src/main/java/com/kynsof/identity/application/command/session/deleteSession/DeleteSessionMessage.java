package com.kynsof.identity.application.command.session.deleteSession;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteSessionMessage implements ICommandMessage {
    private final String userId;
    private final String sessionId;
    private final String command = "DELETE_SESSION";
}
