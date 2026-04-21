package com.kynsof.identity.application.command.auth.syncGoogleUser;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Mensaje de respuesta para la sincronización de usuario de Google.
 */
@Getter
@Setter
public class SyncGoogleUserMessage implements ICommandMessage {

    private UUID userId;
    private String email;
    private String name;
    private String lastName;
    private boolean isNewUser;
    private boolean keyCloakIdUpdated;
    private final String command = "SYNC_GOOGLE_USER";

    public SyncGoogleUserMessage() {
    }

    public SyncGoogleUserMessage(UUID userId, String email, String name, String lastName,
                                  boolean isNewUser, boolean keyCloakIdUpdated) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.isNewUser = isNewUser;
        this.keyCloakIdUpdated = keyCloakIdUpdated;
    }
}
