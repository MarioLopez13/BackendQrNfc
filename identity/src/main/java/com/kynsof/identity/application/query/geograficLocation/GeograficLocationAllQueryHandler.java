package com.kynsof.identity.application.query.geograficLocation;

import com.kynsof.identity.domain.dto.ProvinceDto;
import com.kynsof.identity.domain.interfaces.service.IGeographicLocationService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeograficLocationAllQueryHandler implements IQueryHandler<geograficLocationAllQuery, GeograficLocationAllResponse>  {

    private final IGeographicLocationService service;

    public GeograficLocationAllQueryHandler(IGeographicLocationService service) {
        this.service = service;
    }

    @Override
    public GeograficLocationAllResponse handle(geograficLocationAllQuery query) {
        List<ProvinceDto> response = service.getAllProvincesWithCantonsAndParishes(query.getQuery());
        return new GeograficLocationAllResponse(response);
    }
}
