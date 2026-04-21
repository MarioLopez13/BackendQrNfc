package com.kynsof.identity.application.query.accessbusiness.search;



import com.kynsof.identity.domain.interfaces.service.IAccessBusiessService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

@Component
public class GetSearchAccessBusinessQueryHandler implements IQueryHandler<GetSearchAccessBusinessQuery, PaginatedResponse> {
    private final IAccessBusiessService service;

    public GetSearchAccessBusinessQueryHandler(IAccessBusiessService service) {
        this.service = service;
    }

    @Override
    public PaginatedResponse handle(GetSearchAccessBusinessQuery query) {

        return this.service.search(query.getPageable(),query.getFilter());
    }
}
