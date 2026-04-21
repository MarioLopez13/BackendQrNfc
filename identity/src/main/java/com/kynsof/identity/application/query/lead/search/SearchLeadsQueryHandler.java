package com.kynsof.identity.application.query.lead.search;

import com.kynsof.identity.domain.interfaces.service.ILeadService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

@Component
public class SearchLeadsQueryHandler implements IQueryHandler<SearchLeadsQuery, PaginatedResponse> {

    private final ILeadService leadService;

    public SearchLeadsQueryHandler(ILeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public PaginatedResponse handle(SearchLeadsQuery query) {
        return leadService.search(query.getPageable(), query.getFilterCriteria());
    }
}
