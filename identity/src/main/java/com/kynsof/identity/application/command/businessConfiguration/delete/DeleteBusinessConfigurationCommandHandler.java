package com.kynsof.identity.application.command.businessConfiguration.delete;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para el comando de eliminar configuración de negocio (soft delete)
 */
@Component
public class DeleteBusinessConfigurationCommandHandler implements ICommandHandler<DeleteBusinessConfigurationCommand> {

    private final IBusinessConfigurationService service;

    public DeleteBusinessConfigurationCommandHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public void handle(DeleteBusinessConfigurationCommand command) {
        BusinessConfigurationDto dto = service.findById(command.getId());
        service.delete(dto);
    }
}
