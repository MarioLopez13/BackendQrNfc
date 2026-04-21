package com.kynsof.identity.application.query.advertisementImage.getById;

import com.kynsof.identity.application.query.advertisementImage.search.AdvertisementImageResponse;
import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class FindAdvertisementImageByIdQueryHandler implements IQueryHandler<FindAdvertisementImageByIdQuery, AdvertisementImageResponse> {

    private final IAdvertisementImageService advertisementImageService;

    public FindAdvertisementImageByIdQueryHandler(IAdvertisementImageService advertisementImageService) {
        this.advertisementImageService = advertisementImageService;
    }

    @Override
    public AdvertisementImageResponse handle(FindAdvertisementImageByIdQuery query) {
        AdvertisementImageDto dto = advertisementImageService.findById(query.getId());
        return new AdvertisementImageResponse(dto);
    }
}
