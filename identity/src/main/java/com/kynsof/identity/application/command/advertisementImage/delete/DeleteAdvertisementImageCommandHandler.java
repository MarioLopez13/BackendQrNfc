package com.kynsof.identity.application.command.advertisementImage.delete;

import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteAdvertisementImageCommandHandler implements ICommandHandler<DeleteAdvertisementImageCommand> {

    private final IAdvertisementImageService advertisementImageService;

    public DeleteAdvertisementImageCommandHandler(IAdvertisementImageService advertisementImageService) {
        this.advertisementImageService = advertisementImageService;
    }

    @Override
    public void handle(DeleteAdvertisementImageCommand command) {
        advertisementImageService.delete(command.getId());
    }
}
