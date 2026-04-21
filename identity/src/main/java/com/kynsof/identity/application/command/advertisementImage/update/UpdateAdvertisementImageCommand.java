package com.kynsof.identity.application.command.advertisementImage.update;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateAdvertisementImageCommand implements ICommand {

    private UUID id;
    private String imagenKey;
    private String position;
    private UUID businessId;

    public UpdateAdvertisementImageCommand(UUID id, String imagenKey, String position, UUID businessId) {
        this.id = id;
        this.imagenKey = imagenKey;
        this.position = position;
        this.businessId = businessId;
    }

    public static UpdateAdvertisementImageCommand fromRequest(UUID id, UpdateAdvertisementImageRequest request) {
        return new UpdateAdvertisementImageCommand(
                id,
                request.getImagenKey(),
                request.getPosition(),
                request.getBusinessId()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new UpdateAdvertisementImageMessage();
    }
}
