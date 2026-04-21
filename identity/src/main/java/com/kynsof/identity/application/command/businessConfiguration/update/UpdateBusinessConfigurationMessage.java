package com.kynsof.identity.application.command.businessConfiguration.update;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

/**
 * Mensaje de respuesta al actualizar una configuración de negocio
 */
@Getter
public class UpdateBusinessConfigurationMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "UPDATE_BUSINESS_CONFIGURATION";

    public UpdateBusinessConfigurationMessage(UUID id) {
        this.id = id;
    }
}
