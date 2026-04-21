package com.kynsof.identity.application.query.businessConfiguration.search;

import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

/**
 * Handler para la query de búsqueda paginada
 */
@Component
public class SearchBusinessConfigurationQueryHandler implements IQueryHandler<SearchBusinessConfigurationQuery, PaginatedResponse> {

    private final IBusinessConfigurationService service;

    public SearchBusinessConfigurationQueryHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public PaginatedResponse handle(SearchBusinessConfigurationQuery query) {
        return service.search(query.getPageable(), query.getFilter());
    }
}
