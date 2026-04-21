package com.kynsof.identity.application.command.auth.syncGoogleUser;

import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetRecipientDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetVarDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.entities.UserSystem;
import com.kynsof.identity.infrastructure.repository.query.UserSystemReadDataJPARepository;
import com.kynsof.identity.infrastructure.services.rabbitMq.googlePatient.EventGooglePatientPublisherService;
import com.kynsof.identity.infrastructure.services.rabbitMq.googlePatient.GooglePatientRegisteredDto;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler para sincronizar usuarios de Google que YA EXISTEN en Keycloak
 * con la base de datos local de identity y publicar evento para crear en otros servicios.
 *
 * IMPORTANTE: Este handler NO crea usuarios en Keycloak porque ya existen
 * (fueron creados automáticamente por el flujo de Google OAuth).
 *
 * Flujo:
 * 1. Recibe los datos del usuario ya autenticado (keycloakUserId, email, etc.)
 * 2. Busca usuario en BD local por keyCloakId
 *    - Si existe: retorna información del usuario
 * 3. Si no existe por keyCloakId, busca por email
 *    - Si existe: actualiza keyCloakId y retorna información
 * 4. Si no existe en BD local:
 *    a) Crea usuario en identity (con ID = keycloakId)
 *    b) Publica evento RabbitMQ para crear paciente en: patients, treatments, cirugia, calendar
 */
@Component
@Slf4j
public class SyncGoogleUserCommandHandler implements ICommandHandler<SyncGoogleUserCommand> {

    private final UserSystemReadDataJPARepository userRepository;
    private final IUserSystemService userSystemService;
    private final EventGooglePatientPublisherService googlePatientPublisher;
    private final ICloudBridgesFileService cloudBridgesService;

    public SyncGoogleUserCommandHandler(
            UserSystemReadDataJPARepository userRepository,
            IUserSystemService userSystemService,
            EventGooglePatientPublisherService googlePatientPublisher,
            ICloudBridgesFileService cloudBridgesService
    ) {
        this.userRepository = userRepository;
        this.userSystemService = userSystemService;
        this.googlePatientPublisher = googlePatientPublisher;
        this.cloudBridgesService = cloudBridgesService;
    }

    @Override
    public void handle(SyncGoogleUserCommand command) {
        try {
            UUID keyCloakId = command.getKeycloakUserId();
            String email = command.getEmail();
            String givenName = command.getFirstName();
            String familyName = command.getLastName();
            String preferredUsername = command.getUsername();
            String provider = command.getProvider();

            log.info("[SyncGoogleUser] Procesando sincronización - Email: {}, KeycloakId: {}", email, keyCloakId);

            SyncGoogleUserMessage message = (SyncGoogleUserMessage) command.getMessage();

            // 1. Buscar usuario por keyCloakId en identity
            Optional<UserSystem> existingUserByKeycloakId = userRepository.findByKeyCloakId(keyCloakId);

            if (existingUserByKeycloakId.isPresent()) {
                log.info("[SyncGoogleUser] Usuario encontrado por keyCloakId: {}", existingUserByKeycloakId.get().getId());
                UserSystemDto userDto = existingUserByKeycloakId.get().toAggregate();

                message.setUserId(userDto.getId());
                message.setEmail(userDto.getEmail());
                message.setName(userDto.getName());
                message.setLastName(userDto.getLastName());
                message.setNewUser(false);
                message.setKeyCloakIdUpdated(false);

                return;
            }

            // 2. No existe por keyCloakId - Buscar por email en identity
            Optional<UserSystem> existingUserByEmail = userRepository.findByEmail(email);

            if (existingUserByEmail.isPresent()) {
                log.info("[SyncGoogleUser] Usuario encontrado por email, actualizando keyCloakId. ID: {}",
                        existingUserByEmail.get().getId());

                UserSystemDto userDto = existingUserByEmail.get().toAggregate();

                // Actualizar keyCloakId
                boolean keyCloakIdUpdated = false;
                if (userDto.getKeyCloakId() == null || !userDto.getKeyCloakId().equals(keyCloakId)) {
                    userDto.setKeyCloakId(keyCloakId);
                    userSystemService.update(userDto);
                    keyCloakIdUpdated = true;
                    log.info("[SyncGoogleUser] KeyCloakId actualizado para usuario: {}", userDto.getId());
                }

                message.setUserId(userDto.getId());
                message.setEmail(userDto.getEmail());
                message.setName(userDto.getName());
                message.setLastName(userDto.getLastName());
                message.setNewUser(false);
                message.setKeyCloakIdUpdated(keyCloakIdUpdated);

                return;
            }

            // 3. Usuario no existe en identity - Crear y publicar evento
            log.info("[SyncGoogleUser] Usuario no existe en BD local, procediendo a crear y publicar evento...");
            UUID userId = createUserAndPublishEvent(keyCloakId, email, givenName, familyName, preferredUsername, provider);

            message.setUserId(userId);
            message.setEmail(email);
            message.setName(givenName != null ? givenName : "");
            message.setLastName(familyName != null ? familyName : "");
            message.setNewUser(true);
            message.setKeyCloakIdUpdated(false);

        } catch (IllegalArgumentException e) {
            log.error("[SyncGoogleUser] Error al parsear UUID del keyCloakId: {}", e.getMessage(), e);
            throw new RuntimeException("Formato de keycloakUserId inválido: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[SyncGoogleUser] Error inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar sincronización de usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el usuario en identity y publica evento para crear paciente en otros servicios.
     * El ID del usuario/paciente es el mismo que el keycloakId.
     * NO crea en Keycloak porque el usuario ya existe ahí (vía Google OAuth).
     */
    private UUID createUserAndPublishEvent(UUID keyCloakId, String email, String givenName,
                                            String familyName, String preferredUsername, String provider) {
        String userName = givenName != null ? givenName : "";
        String userLastName = familyName != null ? familyName : "";

        try {
            // 1. Crear usuario en identity con ID = keycloakId
            UserSystemDto userDto = new UserSystemDto(
                    keyCloakId, // ID = keycloakId para mantener consistencia
                    preferredUsername != null ? preferredUsername : email,
                    email,
                    userName,
                    userLastName,
                    UserStatus.ACTIVE,
                    ""
            );
            userDto.setKeyCloakId(keyCloakId);
            userDto.setUserType(EUserType.PATIENTS);

            UUID userId = userSystemService.create(userDto);
            log.info("[SyncGoogleUser] 1. Usuario creado en identity con ID: {}", userId);

            // 2. Publicar evento para crear paciente en patients, treatments, cirugia, calendar
            GooglePatientRegisteredDto event = GooglePatientRegisteredDto.builder()
                    .id(keyCloakId) // El ID del paciente es el mismo que keycloakId
                    .email(email)
                    .name(userName)
                    .lastName(userLastName)
                    .image("") // No hay imagen de identificación
                    .provider(provider != null ? provider : "google")
                    .requiresProfileUpdate(true) // El usuario debe completar su perfil
                    .build();

            googlePatientPublisher.publishGooglePatientRegisteredEvent(event);
            log.info("[SyncGoogleUser] 2. Evento de paciente publicado a RabbitMQ");

            // 3. Enviar correo de bienvenida
            sendWelcomeEmail(email, userName, userLastName, provider);
            log.info("[SyncGoogleUser] 3. Correo de bienvenida enviado");

            return userId;

        } catch (Exception e) {
            log.error("[SyncGoogleUser] Error en createUserAndPublishEvent: {}", e.getMessage());
            throw new RuntimeException("Error al crear usuario y publicar evento: " + e.getMessage(), e);
        }
    }

    /**
     * Envía el correo de bienvenida para usuarios registrados con Google OAuth.
     * A diferencia del registro normal, no incluye contraseña temporal.
     */
    private void sendWelcomeEmail(String email, String name, String lastName, String provider) {
        try {
            SendMailJetEmailRequestDto requestDto = new SendMailJetEmailRequestDto();

            // Configurar destinatario
            List<MailJetRecipientDto> recipients = new ArrayList<>();
            recipients.add(new MailJetRecipientDto(email, name + " " + lastName));
            requestDto.setRecipientEmail(recipients);

            // Configurar variables para la plantilla
            List<MailJetVarDto> vars = new ArrayList<>();
            vars.add(new MailJetVarDto("user_name", email));
            vars.add(new MailJetVarDto("temp_password", "Registrado con " + (provider != null ? provider : "Google")));

            requestDto.setMailJetVars(vars);

            // Configurar el asunto
            requestDto.setSubject("Bienvenido a DoctorKyn - Cuenta creada con Google");

            // ID de la plantilla en Mailjet (misma plantilla que registro normal)
            requestDto.setTemplateId("5965446");

            // Enviar la solicitud
            cloudBridgesService.sendEmail(requestDto);

        } catch (Exception e) {
            // No lanzar excepción si falla el correo, solo log
            log.error("[SyncGoogleUser] Error al enviar correo de bienvenida: {}", e.getMessage());
        }
    }
}
