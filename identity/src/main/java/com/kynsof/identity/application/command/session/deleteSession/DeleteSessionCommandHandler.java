package com.kynsof.identity.application.command.session.deleteSession;

import com.kynsof.identity.infrastructure.services.KeycloakProvider;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;

@Component
@Slf4j
public class DeleteSessionCommandHandler implements ICommandHandler<DeleteSessionCommand> {

    private final KeycloakProvider keycloakProvider;

    public DeleteSessionCommandHandler(KeycloakProvider keycloakProvider) {
        this.keycloakProvider = keycloakProvider;
    }

    @Override
    public void handle(DeleteSessionCommand command) {
        try {
            log.info("Eliminando sesión {} del usuario {}", command.getSessionId(), command.getUserId());

            // Eliminar la sesión específica en Keycloak
            keycloakProvider.withRealm(realm -> {
                realm.deleteSession(command.getSessionId());
                return null;
            });

            log.info("Sesión {} eliminada exitosamente", command.getSessionId());
        } catch (Exception e) {
            log.error("Error al eliminar sesión {} del usuario {}: {}",
                command.getSessionId(), command.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error al eliminar la sesión: " + e.getMessage(), e);
        }
    }
}
