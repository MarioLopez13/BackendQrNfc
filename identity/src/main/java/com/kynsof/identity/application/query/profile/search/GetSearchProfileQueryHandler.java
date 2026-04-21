package com.kynsof.identity.application.query.profile.search;

import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetSearchProfileQueryHandler implements IQueryHandler<GetSearchProfileQuery, PaginatedResponse> {

    private final IProfileService service;

    public GetSearchProfileQueryHandler(IProfileService service) {
        this.service = service;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse handle(GetSearchProfileQuery query) {
        return this.service.search(query.getPageable(), query.getFilter());
    }
}
