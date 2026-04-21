package com.kynsof.identity.application.command.session.deleteAllSessions;

import com.kynsof.identity.infrastructure.services.KeycloakProvider;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeleteAllSessionsCommandHandler implements ICommandHandler<DeleteAllSessionsCommand> {

    private final KeycloakProvider keycloakProvider;

    public DeleteAllSessionsCommandHandler(KeycloakProvider keycloakProvider) {
        this.keycloakProvider = keycloakProvider;
    }

    @Override
    public void handle(DeleteAllSessionsCommand command) {
        try {
            log.info("Eliminando todas las sesiones del usuario {}", command.getUserId());

            // Hacer logout del usuario (elimina todas sus sesiones activas)
            keycloakProvider.withUsers(users -> {
                users.get(command.getUserId()).logout();
                return null;
            });

            log.info("Todas las sesiones del usuario {} fueron eliminadas exitosamente", command.getUserId());
        } catch (Exception e) {
            log.error("Error al eliminar todas las sesiones del usuario {}: {}",
                command.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error al eliminar todas las sesiones: " + e.getMessage(), e);
        }
    }
}
