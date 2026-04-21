package com.kynsof.identity.application.command.businessConfiguration.create;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.identity.domain.rules.businessConfiguration.ConfigKeyCannotBeEmptyRule;
import com.kynsof.identity.domain.rules.businessConfiguration.ConfigKeyMustBeUniqueForBusinessRule;
import com.kynsof.identity.domain.rules.businessConfiguration.ConfigValueCannotBeEmptyRule;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

/**
 * Handler para el comando de crear configuración de negocio
 */
@Component
public class CreateBusinessConfigurationCommandHandler implements ICommandHandler<CreateBusinessConfigurationCommand> {

    private final IBusinessConfigurationService service;

    public CreateBusinessConfigurationCommandHandler(IBusinessConfigurationService service) {
        this.service = service;
    }

    @Override
    public void handle(CreateBusinessConfigurationCommand command) {
        // Validaciones de negocio
        RulesChecker.checkRule(new ConfigKeyCannotBeEmptyRule(command.getConfigKey()));
        RulesChecker.checkRule(new ConfigValueCannotBeEmptyRule(command.getConfigValue()));
        RulesChecker.checkRule(new ConfigKeyMustBeUniqueForBusinessRule(
                service,
                command.getBusinessId(),
                command.getConfigKey(),
                command.getId()
        ));

        // Crear DTO y persistir
        service.create(new BusinessConfigurationDto(
                command.getId(),
                command.getBusinessId(),
                command.getConfigKey(),
                command.getConfigValue(),
                command.getCategory(),
                command.getDataType(),
                command.getDescription(),
                command.getIsEncrypted(),
                true // isActive por defecto true
        ));
    }
}
