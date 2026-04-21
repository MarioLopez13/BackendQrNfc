package com.kynsof.identity.application.command.advertisementImage.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateAdvertisementImageMessage implements ICommandMessage {

    private final UUID id;

    public CreateAdvertisementImageMessage(UUID id) {
        this.id = id;
    }
}
