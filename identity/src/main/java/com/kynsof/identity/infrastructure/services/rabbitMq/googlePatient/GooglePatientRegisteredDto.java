package com.kynsof.identity.infrastructure.services.rabbitMq.googlePatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO para el evento de registro de paciente vía Google OAuth.
 * El ID es el mismo que el keycloakId para mantener consistencia entre servicios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GooglePatientRegisteredDto implements Serializable {

    /**
     * ID del paciente (mismo que keycloakId)
     */
    private UUID id;

    /**
     * Email del usuario (también usado como identification)
     */
    private String email;

    /**
     * Nombre del paciente
     */
    private String name;

    /**
     * Apellido del paciente
     */
    private String lastName;

    /**
     * Imagen de perfil (URL de Google si existe)
     */
    private String image;

    /**
     * Proveedor de autenticación (google, facebook, etc.)
     */
    private String provider;

    /**
     * Indica que el usuario debe completar su perfil
     */
    private boolean requiresProfileUpdate;
}
