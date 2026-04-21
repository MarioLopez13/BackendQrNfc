package com.kynsof.identity.application.command.auth.syncGoogleUser;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

/**
 * Comando para sincronizar usuario de Google que ya existe en Keycloak
 * con la base de datos local de identity.
 *
 * Flujo esperado:
 * 1. El usuario se autentica vía Google OAuth en Keycloak
 * 2. El frontend intercambia el código por tokens
 * 3. El frontend extrae los datos del usuario del token
 * 4. El frontend llama a este endpoint con los datos del usuario
 * 5. El backend crea/actualiza el usuario en la BD local (sin tocar Keycloak)
 */
@Getter
public class SyncGoogleUserCommand implements ICommand {

    private final UUID keycloakUserId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String provider;
    private final String accessToken;
    private final SyncGoogleUserMessage message;

    public SyncGoogleUserCommand(UUID keycloakUserId, String email, String firstName,
                                  String lastName, String username, String provider,
                                  String accessToken) {
        this.keycloakUserId = keycloakUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.provider = provider;
        this.accessToken = accessToken;
        this.message = new SyncGoogleUserMessage();
    }

    public static SyncGoogleUserCommand fromRequest(SyncGoogleUserRequest request) {
        UUID keycloakId = UUID.fromString(request.getKeycloakUserId());
        return new SyncGoogleUserCommand(
            keycloakId,
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getUsername(),
            request.getProvider(),
            request.getAccessToken()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return message;
    }
}
