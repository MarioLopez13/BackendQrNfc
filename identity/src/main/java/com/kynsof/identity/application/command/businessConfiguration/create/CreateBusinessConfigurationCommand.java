package com.kynsof.identity.application.command.businessConfiguration.create;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Comando CQRS para crear una configuración de negocio
 */
@Getter
@Setter
public class CreateBusinessConfigurationCommand implements ICommand {

    private UUID id;
    private UUID businessId;
    private String configKey;
    private String configValue;
    private EConfigCategory category;
    private EConfigDataType dataType;
    private String description;
    private Boolean isEncrypted;

    public CreateBusinessConfigurationCommand(
            UUID businessId,
            String configKey,
            String configValue,
            EConfigCategory category,
            EConfigDataType dataType,
            String description,
            Boolean isEncrypted) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.configKey = configKey;
        this.configValue = configValue;
        this.category = category;
        this.dataType = dataType;
        this.description = description;
        this.isEncrypted = isEncrypted != null ? isEncrypted : false;
    }

    public static CreateBusinessConfigurationCommand fromRequest(CreateBusinessConfigurationRequest request) {
        return new CreateBusinessConfigurationCommand(
                request.getBusinessId(),
                request.getConfigKey(),
                request.getConfigValue(),
                request.getCategory(),
                request.getDataType(),
                request.getDescription(),
                request.getIsEncrypted()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateBusinessConfigurationMessage(id);
    }
}
