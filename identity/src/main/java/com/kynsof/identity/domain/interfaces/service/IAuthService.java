package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.application.command.auth.autenticate.LoginRequest;
import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.application.command.auth.forwardPassword.PasswordChangeRequest;
import com.kynsof.identity.application.command.auth.registry.UserRequest;
import com.kynsof.identity.application.command.auth.registrySystemUser.UserSystemKycloackRequest;
import io.micrometer.common.lang.NonNull;

public interface IAuthService {
    TokenResponse exchangeCodeForToken(String code, String redirectUri);
    TokenResponse authenticate(LoginRequest loginDTO);
    TokenResponse refreshToken(String refreshToken);
    String registerUser(@NonNull UserRequest userRequest, boolean isSystemUser);
    String registerUserSystem(@NonNull UserSystemKycloackRequest userRequest, boolean isSystemUser);
    Boolean forwardPassword(PasswordChangeRequest changeRequest);
    Boolean sendPasswordRecoveryOtp(String email);
    Boolean changePassword(String userId, String newPassword,boolean temporary);
    Boolean firstChangePassword(String userId, String email, String newPassword, String oldPassword);
    Boolean delete(String userId);
    void updateUser(String userId, UserRequest userRequest);

    /**
     * Genera un token JWT para un usuario específico usando Token Exchange de Keycloak.
     * Esto permite autenticar a un usuario sin necesidad de su contraseña,
     * utilizando el service account del cliente.
     *
     * @param userId El ID de Keycloak del usuario
     * @return TokenResponse con access_token y refresh_token
     */
    TokenResponse tokenExchangeForUser(String userId);
}
