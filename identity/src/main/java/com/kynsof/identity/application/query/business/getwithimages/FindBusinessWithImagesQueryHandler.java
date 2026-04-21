package com.kynsof.identity.application.query.business.getwithimages;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindBusinessWithImagesQueryHandler implements IQueryHandler<FindBusinessWithImagesQuery, BusinessWithImagesResponse> {

    private final IBusinessService businessService;
    private final IAdvertisementImageService advertisementImageService;

    public FindBusinessWithImagesQueryHandler(IBusinessService businessService,
                                              IAdvertisementImageService advertisementImageService) {
        this.businessService = businessService;
        this.advertisementImageService = advertisementImageService;
    }

    @Override
    public BusinessWithImagesResponse handle(FindBusinessWithImagesQuery query) {
        BusinessDto business = businessService.findById(query.getBusinessId());
        List<AdvertisementImageDto> images = advertisementImageService.findByBusinessId(query.getBusinessId());

        return new BusinessWithImagesResponse(business, images);
    }
}
