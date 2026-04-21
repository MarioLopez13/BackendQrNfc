package com.kynsof.identity.application.command.advertisementImage.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateAdvertisementImageCommand implements ICommand {

    private UUID id;
    private String imagenKey;
    private String position;
    private UUID businessId;

    public CreateAdvertisementImageCommand(String imagenKey, String position, UUID businessId) {
        this.id = UUID.randomUUID();
        this.imagenKey = imagenKey;
        this.position = position;
        this.businessId = businessId;
    }

    public static CreateAdvertisementImageCommand fromRequest(CreateAdvertisementImageRequest request) {
        return new CreateAdvertisementImageCommand(
                request.getImagenKey(),
                request.getPosition(),
                request.getBusinessId()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateAdvertisementImageMessage(id);
    }
}
