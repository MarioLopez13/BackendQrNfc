package com.kynsof.identity.application.command.advertisementImage.update;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateAdvertisementImageCommandHandler implements ICommandHandler<UpdateAdvertisementImageCommand> {

    private final IAdvertisementImageService advertisementImageService;
    private final IBusinessService businessService;

    public UpdateAdvertisementImageCommandHandler(IAdvertisementImageService advertisementImageService,
                                                   IBusinessService businessService) {
        this.advertisementImageService = advertisementImageService;
        this.businessService = businessService;
    }

    @Override
    public void handle(UpdateAdvertisementImageCommand command) {
        // Validate advertisement image exists
        AdvertisementImageDto dto = advertisementImageService.findById(command.getId());

        // Validate business exists
        businessService.findById(command.getBusinessId());

        // Update fields
        dto.setImagenKey(command.getImagenKey());
        dto.setPosition(command.getPosition());
        dto.setBusinessId(command.getBusinessId());

        advertisementImageService.update(dto);
    }
}
