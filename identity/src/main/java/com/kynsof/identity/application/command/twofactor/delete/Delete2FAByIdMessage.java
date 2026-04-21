package com.kynsof.identity.application.command.twofactor.delete;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Delete2FAByIdMessage implements ICommandMessage {

    private UUID id;

    public static Delete2FAByIdMessage success(UUID id) {
        return new Delete2FAByIdMessage(id);
    }
}
