package com.kynsof.identity.application.command.auth.autenticate;

import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.identity.infrastructure.services.RedisOtpService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.exception.AuthenticateNotFoundException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AuthenticateCommandHandler implements ICommandHandler<AuthenticateCommand> {
    private final IAuthService authService;
    private final ITwoFactorAuthService twoFactorAuthService;

    public AuthenticateCommandHandler(IAuthService authService,
                                      ICloudBridgesFileService cloudBridgesService,
                                      RedisOtpService redisOtpService,
                                      ITwoFactorAuthService twoFactorAuthService) {
        this.authService = authService;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void handle(AuthenticateCommand command) {
        try {
            LoginRequest loginRequest = new LoginRequest(command.getUserName(), command.getPassword());
            TokenResponse tokenResponse = authService.authenticate(loginRequest);

            // Extraer userId del token y verificar si tiene 2FA habilitado
            if (tokenResponse.getAccessToken() != null) {
                try {
                    JWT jwt = JWTParser.parse(tokenResponse.getAccessToken());
                    String userId = jwt.getJWTClaimsSet().getSubject();

                    // Siempre establecer el userId en el response
                    if (userId != null) {
                        tokenResponse.setUserId(userId);

                        // Verificar si el usuario tiene 2FA habilitado
                        if (twoFactorAuthService.isTwoFactorEnabled(UUID.fromString(userId))) {
                            // Usuario tiene 2FA, marcar que se requiere verificación
                            tokenResponse.setTwoFactorRequired(true);
                            // El frontend debe llamar a /api/2fa/verify-login para completar el proceso
                            log.info("Usuario {} requiere verificación 2FA", userId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error procesando token para usuario: {}", e.getMessage());
                }
            }

            command.setTokenResponse(tokenResponse);
            } catch (AuthenticateNotFoundException ex) {
            TokenResponse errorResponse = new TokenResponse();
            errorResponse.setError("invalid_credentials");
            errorResponse.setErrorDescription("El usuario o contraseña es incorrecto. Por favor intente de nuevo.");
            errorResponse.setAuthenticated(false);
            errorResponse.setErrorField("email/password");
            errorResponse.setErrorMessage("The username or password is incorrect. Please try again.");
            command.setTokenResponse(errorResponse);
        } catch (Exception ex) {
            // Respuesta para errores inesperados
            TokenResponse errorResponse = new TokenResponse();
            errorResponse.setError("authentication_error");
            errorResponse.setErrorDescription("Error al procesar la solicitud de autenticación. Por favor, inténtelo más tarde.");
            errorResponse.setAuthenticated(false);
            errorResponse.setErrorField("system");
            errorResponse.setErrorMessage("An unexpected error occurred. Please try again later.");
            command.setTokenResponse(errorResponse);
        }



    }
}
