package com.kynsof.identity.application.command.auth.registerGoogle;

import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.repository.query.UserSystemReadDataJPARepository;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Handler para registrar/sincronizar usuarios que se autentican vía Google OAuth2.
 *
 * Flujo:
 * 1. Decodifica el JWT access_token para extraer información del usuario
 * 2. Verifica si el usuario ya existe en BD local (por keyCloakId)
 * 3. Si no existe, lo crea automáticamente con rol PATIENTS
 * 4. Retorna información del usuario (nuevo o existente)
 */
@Component
@Slf4j
public class RegisterGoogleUserCommandHandler implements ICommandHandler<RegisterGoogleUserCommand> {

    private final JwtDecoder jwtDecoder;
    private final UserSystemReadDataJPARepository userRepository;
    private final IUserSystemService userSystemService;
    private final IAuthService authService;

    public RegisterGoogleUserCommandHandler(
            JwtDecoder jwtDecoder,
            UserSystemReadDataJPARepository userRepository,
            IUserSystemService userSystemService,
            IAuthService authService
    ) {
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.userSystemService = userSystemService;
        this.authService = authService;
    }

    @Override
    public void handle(RegisterGoogleUserCommand command) {
        try {
            // 1. Intercambiar código de Google por tokens con Keycloak
            log.info("Intercambiando código de Google con Keycloak");
            TokenResponse tokenResponse = authService.exchangeCodeForToken(command.getCode(), command.getRedirectUri());

            // 2. Validar que el token fue obtenido correctamente
            if (tokenResponse.getError() != null) {
                log.error("Error al intercambiar código: {}", tokenResponse.getError());
                throw new RuntimeException("Error al validar con Google: " + tokenResponse.getErrorDescription());
            }

            // 3. Decodificar el JWT para obtener los claims del usuario
            String accessToken = tokenResponse.getAccessToken();
            Jwt jwt = jwtDecoder.decode(accessToken);

            // 4. Extraer información del token
            String keyCloakIdString = jwt.getSubject();
            UUID keyCloakId = UUID.fromString(keyCloakIdString);
            String email = jwt.getClaimAsString("email");
            String givenName = jwt.getClaimAsString("given_name");
            String familyName = jwt.getClaimAsString("family_name");
            String preferredUsername = jwt.getClaimAsString("preferred_username");

            log.info("Procesando registro de usuario Google - Email: {}, KeycloakId: {}", email, keyCloakId);

            // 5. Verificar si el usuario ya existe en BD local por keyCloakId
            Optional<com.kynsof.identity.infrastructure.entities.UserSystem> existingUser =
                userRepository.findByKeyCloakId(keyCloakId);

            if (existingUser.isPresent()) {
                // Usuario ya existe, retornar su información
                log.info("Usuario ya existe en BD local con ID: {}", existingUser.get().getId());
                UserSystemDto userDto = existingUser.get().toAggregate();

                RegisterGoogleUserMessage message = (RegisterGoogleUserMessage) command.getMessage();
                message.setUserId(userDto.getId());
                message.setEmail(userDto.getEmail());
                message.setName(userDto.getName());
                message.setLastName(userDto.getLastName());
                message.setNewUser(false);

                return;
            }

            // 6. Usuario nuevo - Crear en identity
            UUID userId = createNewGoogleUser(keyCloakId, email, givenName, familyName, preferredUsername);

            // 7. Retornar información del usuario creado
            RegisterGoogleUserMessage message = (RegisterGoogleUserMessage) command.getMessage();
            message.setUserId(userId);
            message.setEmail(email);
            message.setName(givenName != null ? givenName : "");
            message.setLastName(familyName != null ? familyName : "");
            message.setNewUser(true);

        } catch (JwtException e) {
            log.error("Error al decodificar JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Token inválido o expirado: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Error al parsear UUID del keyCloakId: {}", e.getMessage(), e);
            throw new RuntimeException("Formato de token inválido: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al registrar usuario de Google: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar registro de usuario: " + e.getMessage(), e);
        }
    }

    private UUID createNewGoogleUser(UUID keyCloakId, String email, String givenName,
                                     String familyName, String preferredUsername) {
        log.info("Creando nuevo usuario Google - Email: {}", email);

        UserSystemDto newUserDto = new UserSystemDto(
            keyCloakId,
            preferredUsername != null ? preferredUsername : email,
            email,
            givenName != null ? givenName : "",
            familyName != null ? familyName : "",
            UserStatus.ACTIVE,
            ""
        );
        newUserDto.setKeyCloakId(keyCloakId);
        newUserDto.setUserType(EUserType.PATIENTS);

        UUID userId = userSystemService.create(newUserDto);

        log.info("Usuario Google creado exitosamente con ID: {}", userId);
        return userId;
    }
}
