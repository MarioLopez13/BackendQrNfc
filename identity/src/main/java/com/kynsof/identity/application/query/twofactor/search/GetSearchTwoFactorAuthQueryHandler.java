package com.kynsof.identity.application.query.twofactor.search;

import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

@Component
public class GetSearchTwoFactorAuthQueryHandler implements IQueryHandler<GetSearchTwoFactorAuthQuery, PaginatedResponse> {

    private final ITwoFactorAuthService service;

    public GetSearchTwoFactorAuthQueryHandler(ITwoFactorAuthService service) {
        this.service = service;
    }

    @Override
    public PaginatedResponse handle(GetSearchTwoFactorAuthQuery query) {
        return this.service.search(query.getPageable(), query.getFilter());
    }
}
