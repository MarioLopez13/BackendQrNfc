package com.kynsof.identity.application.command.auth.emailLogin.verify;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IRedisService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.exception.BusinessException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.UserNotFoundException;
import com.kynsof.share.core.domain.response.ErrorField;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailLoginVerifyCommandHandler implements ICommandHandler<EmailLoginVerifyCommand> {

    private final IRedisService redisService;
    private final IUserSystemService userSystemService;
    private final IAuthService authService;

    public EmailLoginVerifyCommandHandler(IRedisService redisService,
                                          IUserSystemService userSystemService,
                                          IAuthService authService) {
        this.redisService = redisService;
        this.userSystemService = userSystemService;
        this.authService = authService;
    }

    @Override
    public void handle(EmailLoginVerifyCommand command) {
        try {
            // 1. Verificar que el usuario existe
            UserSystemDto userSystemDto = userSystemService.findByEmail(command.getEmail());
            if (userSystemDto == null) {
                throw new UserNotFoundException("Usuario no encontrado",
                    new ErrorField("email", "No existe un usuario con este correo electrónico"));
            }

            // 2. Verificar que el usuario tiene keycloakId
            if (userSystemDto.getKeyCloakId() == null) {
                log.error("User {} does not have a keycloakId", command.getEmail());
                throw new BusinessException(DomainErrorMessage.USER_NOT_FOUND,
                    "El usuario no tiene un ID de Keycloak asociado");
            }

            // 3. Obtener el OTP almacenado en Redis
            String storedOtp = redisService.getOtpCode(command.getEmail());
            if (storedOtp == null) {
                log.warn("No OTP found in Redis for email: {}", command.getEmail());
                throw new BusinessException(DomainErrorMessage.OTP_EXPIRED,
                    "El código ha expirado. Solicite uno nuevo.");
            }

            // 4. Validar que el código coincide
            if (!storedOtp.equals(command.getCode())) {
                log.warn("Invalid OTP provided for email: {}", command.getEmail());
                throw new BusinessException(DomainErrorMessage.OTP_INVALID,
                    "El código ingresado es incorrecto");
            }

            // 5. Eliminar el OTP usado (single use)
            redisService.deleteKey(command.getEmail());

            // 6. Realizar Token Exchange para obtener el token del usuario
            // Convertir UUID a String para el método tokenExchangeForUser
            TokenResponse tokenResponse = authService.tokenExchangeForUser(
                userSystemDto.getKeyCloakId().toString()
            );

            // 7. Extraer userId del JWT y establecerlo en el response
            try {
                JWT jwt = JWTParser.parse(tokenResponse.getAccessToken());
                String userId = jwt.getJWTClaimsSet().getSubject();
                if (userId != null) {
                    tokenResponse.setUserId(userId);
                }
            } catch (Exception e) {
                log.warn("Could not extract userId from JWT: {}", e.getMessage());
            }

            // 8. Establecer la respuesta
            command.setTokenResponse(tokenResponse);

            log.info("Email login verification successful for user: {}", command.getEmail());

        } catch (UserNotFoundException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during email login verification for: {}", command.getEmail(), e);
            throw new RuntimeException("Error al verificar el código de inicio de sesión: " + e.getMessage());
        }
    }
}
