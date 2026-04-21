package com.kynsof.identity.application.command.businessConfiguration.delete;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Comando CQRS para eliminar (soft delete) una configuración de negocio
 */
@Getter
@Setter
@AllArgsConstructor
public class DeleteBusinessConfigurationCommand implements ICommand {

    private UUID id;

    @Override
    public ICommandMessage getMessage() {
        return new DeleteBusinessConfigurationMessage(id);
    }
}
