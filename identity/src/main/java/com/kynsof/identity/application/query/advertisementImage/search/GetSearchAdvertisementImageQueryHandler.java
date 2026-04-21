package com.kynsof.identity.application.query.advertisementImage.search;

import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

@Component
public class GetSearchAdvertisementImageQueryHandler implements IQueryHandler<GetSearchAdvertisementImageQuery, PaginatedResponse> {

    private final IAdvertisementImageService advertisementImageService;

    public GetSearchAdvertisementImageQueryHandler(IAdvertisementImageService advertisementImageService) {
        this.advertisementImageService = advertisementImageService;
    }

    @Override
    public PaginatedResponse handle(GetSearchAdvertisementImageQuery query) {
        return advertisementImageService.search(query.getPageable(), query.getFilter());
    }
}
