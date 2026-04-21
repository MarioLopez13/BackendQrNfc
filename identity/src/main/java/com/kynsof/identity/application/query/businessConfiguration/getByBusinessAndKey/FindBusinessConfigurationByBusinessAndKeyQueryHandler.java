package com.kynsof.identity.application.query.businessConfiguration.getByBusinessAndKey;

import com.kynsof.identity.application.query.businessConfiguration.getById.BusinessConfigurationResponse;
import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para la query de buscar configuración por businessId y configKey
 */
@Component
public class FindBusinessConfigurationByBusinessAndKeyQueryHandler implements IQueryHandler<FindBusinessConfigurationByBusinessAndKeyQuery, BusinessConfigurationResponse> {

    private final IBusinessConfigurationService service;

    public FindBusinessConfigurationByBusinessAndKeyQueryHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public BusinessConfigurationResponse handle(FindBusinessConfigurationByBusinessAndKeyQuery query) {
        BusinessConfigurationDto dto = service.findByBusinessIdAndConfigKey(
                query.getBusinessId(),
                query.getConfigKey()
        );
        return new BusinessConfigurationResponse(dto);
    }
}
