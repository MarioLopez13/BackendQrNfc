package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.TwoFactorAuthDto;
import com.kynsof.identity.domain.dto.TwoFactorSetupDto;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ITwoFactorAuthService {

    /**
     * Inicia el setup de 2FA para un usuario
     * @param userId ID del usuario
     * @param email Email del usuario (para mostrar en Google Authenticator)
     * @return Setup con secreto y QR code en base64
     */
    TwoFactorSetupDto setupTwoFactor(UUID userId, String email);

    /**
     * Verifica el codigo TOTP y activa 2FA
     * @param userId ID del usuario
     * @param code Codigo de 6 digitos
     * @return Lista de codigos de respaldo si es exitoso
     */
    String[] verifyAndEnableTwoFactor(UUID userId, String code);

    /**
     * Verifica un codigo TOTP durante el login
     * @param userId ID del usuario
     * @param code Codigo de 6 digitos
     * @return true si el codigo es valido
     */
    boolean verifyCode(UUID userId, String code);

    /**
     * Verifica un codigo de respaldo
     * @param userId ID del usuario
     * @param backupCode Codigo de respaldo
     * @return true si el codigo es valido y se consumio
     */
    boolean verifyBackupCode(UUID userId, String backupCode);

    /**
     * Desactiva 2FA para un usuario
     * @param userId ID del usuario
     * @param code Codigo TOTP actual para confirmar
     */
    void disableTwoFactor(UUID userId, String code);

    /**
     * Verifica si el usuario tiene 2FA activado
     * @param userId ID del usuario
     * @return true si 2FA esta activado
     */
    boolean isTwoFactorEnabled(UUID userId);

    /**
     * Obtiene el estado de 2FA de un usuario
     * @param userId ID del usuario
     * @return DTO con la informacion de 2FA o empty si no existe
     */
    Optional<TwoFactorAuthDto> getTwoFactorStatus(UUID userId);

    /**
     * Regenera los codigos de respaldo
     * @param userId ID del usuario
     * @param code Codigo TOTP para confirmar
     * @return Nuevos codigos de respaldo
     */
    String[] regenerateBackupCodes(UUID userId, String code);

    /**
     * Busca registros de 2FA con paginacion y filtros
     * @param pageable Paginacion
     * @param filter Filtros de busqueda
     * @return Respuesta paginada
     */
    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filter);

    /**
     * Elimina un registro de 2FA por ID (admin)
     * @param id ID del registro de 2FA
     */
    void deleteById(UUID id);

    /**
     * Obtiene un registro de 2FA por ID
     * @param id ID del registro
     * @return DTO con la informacion de 2FA
     */
    Optional<TwoFactorAuthDto> getById(UUID id);
}
