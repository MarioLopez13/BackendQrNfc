package com.kynsof.identity.application.query.businessConfiguration.getById;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para la query de buscar configuración por ID
 */
@Component
public class FindBusinessConfigurationByIdQueryHandler implements IQueryHandler<FindBusinessConfigurationByIdQuery, BusinessConfigurationResponse> {

    private final IBusinessConfigurationService service;

    public FindBusinessConfigurationByIdQueryHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public BusinessConfigurationResponse handle(FindBusinessConfigurationByIdQuery query) {
        BusinessConfigurationDto dto = service.findById(query.getId());
        return new BusinessConfigurationResponse(dto);
    }
}
