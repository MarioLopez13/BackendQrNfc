package com.kynsof.identity.application.command.advertisementImage.create;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateAdvertisementImageCommandHandler implements ICommandHandler<CreateAdvertisementImageCommand> {

    private final IAdvertisementImageService advertisementImageService;
    private final IBusinessService businessService;

    public CreateAdvertisementImageCommandHandler(IAdvertisementImageService advertisementImageService,
                                                   IBusinessService businessService) {
        this.advertisementImageService = advertisementImageService;
        this.businessService = businessService;
    }

    @Override
    public void handle(CreateAdvertisementImageCommand command) {
        // Validate business exists
        businessService.findById(command.getBusinessId());

        AdvertisementImageDto dto = new AdvertisementImageDto(
                command.getId(),
                command.getImagenKey(),
                command.getPosition(),
                command.getBusinessId()
        );

        UUID id = advertisementImageService.create(dto);
        command.setId(id);
    }
}
