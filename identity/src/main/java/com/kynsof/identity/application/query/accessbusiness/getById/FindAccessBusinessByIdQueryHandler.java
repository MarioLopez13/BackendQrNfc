package com.kynsof.identity.application.query.accessbusiness.getById;

import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.identity.domain.interfaces.service.IAccessBusiessService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class FindAccessBusinessByIdQueryHandler implements IQueryHandler<FindAccessBusinessByIdQuery, AccessBusinessResponse> {

    private final IAccessBusiessService service;

    public FindAccessBusinessByIdQueryHandler(IAccessBusiessService service) {
        this.service = service;
    }

    @Override
    public AccessBusinessResponse handle(FindAccessBusinessByIdQuery query) {
        AccessBusinessDto response = service.findById(query.getId());

        return new AccessBusinessResponse(response);
    }
}