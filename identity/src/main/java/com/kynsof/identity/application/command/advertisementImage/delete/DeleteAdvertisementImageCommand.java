package com.kynsof.identity.application.command.advertisementImage.delete;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DeleteAdvertisementImageCommand implements ICommand {

    private UUID id;

    public DeleteAdvertisementImageCommand(UUID id) {
        this.id = id;
    }

    @Override
    public ICommandMessage getMessage() {
        return new DeleteAdvertisementImageMessage();
    }
}
