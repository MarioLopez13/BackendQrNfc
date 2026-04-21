package com.kynsof.identity.application.query.business.getbyid.httpReplicate;

import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class FindBusinessByIdReplicateHttpQueryHandler implements IQueryHandler<FindBusinessByIdReplicateHttpQuery, BusinessHttp>  {

    private final IBusinessService service;

    public FindBusinessByIdReplicateHttpQueryHandler(IBusinessService service) {
        this.service = service;
    }

    @Override
    public BusinessHttp handle(FindBusinessByIdReplicateHttpQuery query) {
        BusinessDto object = service.findById(query.getId());

        BusinessHttp response = new BusinessHttp();
        response.setId(object.getId());
        response.setAddress(object.getAddress());
        response.setEmail(object.getEmail());
        response.setLatitude(object.getLatitude());
        response.setLongitude(object.getLongitude());
        response.setLogo(object.getLogo());
        response.setName(object.getName());
        response.setRuc(object.getRuc());
        response.setPhone(object.getPhone());

        return response;
    }
}
