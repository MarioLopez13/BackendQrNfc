package com.kynsof.identity.application.query.businessConfiguration.getByKey;

import com.kynsof.identity.application.query.businessConfiguration.getById.BusinessConfigurationResponse;
import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para la query de buscar configuración solo por configKey
 */
@Component
public class FindBusinessConfigurationByKeyQueryHandler implements IQueryHandler<FindBusinessConfigurationByKeyQuery, BusinessConfigurationResponse> {

    private final IBusinessConfigurationService service;

    public FindBusinessConfigurationByKeyQueryHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public BusinessConfigurationResponse handle(FindBusinessConfigurationByKeyQuery query) {
        BusinessConfigurationDto dto = service.findByConfigKey(query.getConfigKey());
        return new BusinessConfigurationResponse(dto);
    }
}
