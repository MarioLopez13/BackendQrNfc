package com.kynsof.identity.application.command.businessConfiguration.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

/**
 * Mensaje de respuesta al crear una configuración de negocio
 */
@Getter
public class CreateBusinessConfigurationMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "CREATE_BUSINESS_CONFIGURATION";

    public CreateBusinessConfigurationMessage(UUID id) {
        this.id = id;
    }
}
