package com.kynsof.identity.application.command.auth.syncGoogleUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para sincronizar usuario de Google que ya existe en Keycloak.
 *
 * Este request se usa cuando el usuario ya se autenticó vía Google OAuth en Keycloak
 * y el frontend ya intercambió el código por tokens. Solo necesita sincronizar
 * el usuario en la base de datos local de identity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncGoogleUserRequest {
    /**
     * ID del usuario en Keycloak (sub claim del JWT)
     */
    private String keycloakUserId;

    /**
     * Email del usuario
     */
    private String email;

    /**
     * Nombre del usuario
     */
    private String firstName;

    /**
     * Apellido del usuario
     */
    private String lastName;

    /**
     * Username preferido (opcional)
     */
    private String username;

    /**
     * Proveedor de autenticación (google, facebook, etc.)
     */
    private String provider;

    /**
     * Token de acceso para llamadas a otros servicios
     */
    private String accessToken;
}