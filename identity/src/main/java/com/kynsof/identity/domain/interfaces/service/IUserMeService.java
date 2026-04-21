package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.application.query.users.userMe.UserMeResponse;

import java.util.UUID;

public interface IUserMeService {
    UserMeResponse getUserInfo(UUID userId);

    /**
     * Obtiene información del usuario con permisos filtrados por businessId.
     * Mejora el rendimiento al evitar cargar todos los negocios y permisos.
     */
    UserMeResponse getUserInfo(UUID userId, UUID businessId);
}
