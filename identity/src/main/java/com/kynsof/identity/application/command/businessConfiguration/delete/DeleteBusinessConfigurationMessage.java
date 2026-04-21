package com.kynsof.identity.application.command.businessConfiguration.delete;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

/**
 * Mensaje de respuesta al eliminar una configuración de negocio
 */
@Getter
public class DeleteBusinessConfigurationMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "DELETE_BUSINESS_CONFIGURATION";

    public DeleteBusinessConfigurationMessage(UUID id) {
        this.id = id;
    }
}
