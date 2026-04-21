package com.kynsof.identity.application.command.session.deleteAllSessions;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteAllSessionsMessage implements ICommandMessage {
    private final String userId;
    private final String command = "DELETE_ALL_SESSIONS";
}
