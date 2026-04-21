package com.kynsof.identity.application.command.businessConfiguration.update;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para el comando de actualizar configuración de negocio
 */
@Component
public class UpdateBusinessConfigurationCommandHandler implements ICommandHandler<UpdateBusinessConfigurationCommand> {

    private final IBusinessConfigurationService service;

    public UpdateBusinessConfigurationCommandHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public void handle(UpdateBusinessConfigurationCommand command) {
        // Obtener la configuración existente
        BusinessConfigurationDto dto = service.findById(command.getId());

        // Actualizar solo los campos proporcionados
        if (command.getConfigValue() != null) {
            dto.setConfigValue(command.getConfigValue());
        }
        if (command.getCategory() != null) {
            dto.setCategory(command.getCategory());
        }
        if (command.getDataType() != null) {
            dto.setDataType(command.getDataType());
        }
        if (command.getDescription() != null) {
            dto.setDescription(command.getDescription());
        }
        if (command.getIsEncrypted() != null) {
            dto.setIsEncrypted(command.getIsEncrypted());
        }
        if (command.getIsActive() != null) {
            dto.setIsActive(command.getIsActive());
        }

        // Persistir cambios
        service.update(dto);
    }
}
