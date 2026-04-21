package com.kynsof.identity.application.command.businessConfiguration.update;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Comando CQRS para actualizar una configuración de negocio
 */
@Getter
@Setter
public class UpdateBusinessConfigurationCommand implements ICommand {

    private UUID id;
    private String configValue;
    private EConfigCategory category;
    private EConfigDataType dataType;
    private String description;
    private Boolean isEncrypted;
    private Boolean isActive;

    public UpdateBusinessConfigurationCommand(
            UUID id,
            String configValue,
            EConfigCategory category,
            EConfigDataType dataType,
            String description,
            Boolean isEncrypted,
            Boolean isActive) {
        this.id = id;
        this.configValue = configValue;
        this.category = category;
        this.dataType = dataType;
        this.description = description;
        this.isEncrypted = isEncrypted;
        this.isActive = isActive;
    }

    public static UpdateBusinessConfigurationCommand fromRequest(UpdateBusinessConfigurationRequest request, UUID id) {
        return new UpdateBusinessConfigurationCommand(
                id,
                request.getConfigValue(),
                request.getCategory(),
                request.getDataType(),
                request.getDescription(),
                request.getIsEncrypted(),
                request.getIsActive()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new UpdateBusinessConfigurationMessage(id);
    }
}
