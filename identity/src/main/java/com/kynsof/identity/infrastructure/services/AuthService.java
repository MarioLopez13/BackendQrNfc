package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.command.auth.autenticate.LoginRequest;
import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.application.command.auth.forwardPassword.PasswordChangeRequest;
import com.kynsof.identity.application.command.auth.registry.UserRequest;
import com.kynsof.identity.application.command.auth.registrySystemUser.UserSystemKycloackRequest;
import com.kynsof.identity.domain.dto.mailjet.MailJetRecipientDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetVarDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import com.kynsof.identity.domain.interfaces.service.IRedisService;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.exception.*;
import com.kynsof.share.core.domain.response.ApiError;
import com.kynsof.share.core.domain.response.ErrorField;
import io.micrometer.common.lang.NonNull;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AuthService implements IAuthService {

    /**
     * Intercambia un código de autorización OAuth2 por un access_token de
     * Keycloak.
     *
     * @param code Código recibido en el redirect de Keycloak
     * @param redirectUri Debe coincidir con el registrado en Keycloak
     * @return TokenResponse con access_token y refresh_token
     */
    public TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", keycloakProvider.getClient_id());
        params.add("client_secret", keycloakProvider.getClient_secret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakProvider.getTokenUri(),
                    HttpMethod.POST,
                    request,
                    TokenResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CustomUnauthorizedException(
                        "No se pudo intercambiar el código por un token.",
                        new ErrorField("code", "Token exchange failed")
                );
            }
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String body = ex.getResponseBodyAsString();
            throw new CustomUnauthorizedException(
                    "No autorizado: código inválido o expirado.",
                    new ErrorField("code", !body.isBlank() ? body : "Code not valid")
            );
        } catch (RestClientException ex) {
            throw new CustomUnauthorizedException(
                    "Servidor de autenticación no disponible.",
                    new ErrorField("auth", ex.getMessage())
            );
        }
    }

    private final KeycloakProvider keycloakProvider;
    private final RestTemplate restTemplate;
    private final IRedisService otpService;
    //   private final OtpMessageProducer otpMessageProducer;
    private final ICloudBridgesFileService cloudBridgesService;

    @Autowired
    public AuthService(KeycloakProvider keycloakProvider, RestTemplate restTemplate,
            IRedisService otpService, ICloudBridgesFileService cloudBridgesService) {
        this.keycloakProvider = keycloakProvider;
        this.restTemplate = restTemplate;
        this.otpService = otpService;
        // this.otpMessageProducer = otpMessageProducer;
        this.cloudBridgesService = cloudBridgesService;
    }

    @Override
    public TokenResponse authenticate(LoginRequest loginDTO) {
        MultiValueMap<String, String> map = createAuthRequestMap(loginDTO.getUsername(), loginDTO.getPassword());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, createHeaders());

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakProvider.getTokenUri(),
                    HttpMethod.POST,
                    entity,
                    TokenResponse.class);
            return handleAuthResponse(response);
        } catch (HttpClientErrorException e) {
            handleAuthException(e, loginDTO.getUsername(), loginDTO.getUsername());
            return null;
        }
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) throws CustomUnauthorizedException {
        // Cuerpo x-www-form-urlencoded
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", keycloakProvider.getClient_id());
        form.add("refresh_token", refreshToken);

        // Solo agrega client_secret si tu cliente es confidencial
        if (keycloakProvider.getClient_secret() != null && !keycloakProvider.getClient_secret().isBlank()) {
            form.add("client_secret", keycloakProvider.getClient_secret());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakProvider.getTokenUri(), // /realms/{realm}/protocol/openid-connect/token
                    HttpMethod.POST,
                    request,
                    TokenResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CustomUnauthorizedException(
                        "Unauthorized: refresh token exchange failed.",
                        new ErrorField("token", "Refresh flow did not return a token")
                );
            }
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Keycloak suele responder JSON con error_description
            String body = ex.getResponseBodyAsString();
            throw new CustomUnauthorizedException(
                    "Unauthorized: Refresh token is invalid or expired.",
                    new ErrorField("token", !body.isBlank() ? body : "Refresh token not found")
            );
        } catch (RestClientException ex) {
            throw new CustomUnauthorizedException(
                    "Auth server not reachable.",
                    new ErrorField("auth", ex.getMessage())
            );
        }
    }

    @Override
    public String registerUser(@NonNull UserRequest userRequest, boolean isSystemUser) {
    return createUser(
            userRequest.getName(),
            userRequest.getLastName(),
            userRequest.getEmail(),
            userRequest.getUserName(),
            userRequest.getPassword(),
            "USER"
        );
    }

    @Override
    public String registerUserSystem(@NonNull UserSystemKycloackRequest userRequest, boolean isSystemUser) {
        return createUser(userRequest.getName(), userRequest.getLastName(), userRequest.getEmail(),
                userRequest.getUserName(), userRequest.getPassword(), userRequest.getUserType().toString().toUpperCase());
    }

    @Override
    public Boolean sendPasswordRecoveryOtp(String email) {
        return keycloakProvider.withUsers(usersResource -> {
            List<UserRepresentation> users = usersResource.searchByEmail(email, true);

            if (!users.isEmpty()) {
                UserRepresentation user = users.getFirst();
                String otpCode = otpService.generateOtpCode();
                otpService.saveOtpCode(email, otpCode);
                String name = user.getFirstName() + " " + user.getLastName();
                sendEmailOtp(email, name);
                return true;
            }
            throw new UserNotFoundException("User not found", new ErrorField("email", "Email not found"));
        });
    }

    @Override
    public Boolean forwardPassword(PasswordChangeRequest changeRequest) {
        String storedOtp = otpService.getOtpCode(changeRequest.getEmail());
        if (storedOtp == null || !storedOtp.equals(changeRequest.getOtp())) {
            // Maneja ambos casos: OTP nulo (expirado) y OTP incorrecto
            ApiError apiError = new ApiError(
                    "El código OTP es inválido o ha expirado.", // Mensaje claro
                    List.of(new ErrorField("otp", "El código OTP es inválido o ha expirado."))
            );
            throw new CustomException(apiError, HttpStatus.BAD_REQUEST);
            //return false;
        }

        return keycloakProvider.withUsers(usersResource -> {
            List<UserRepresentation> users = usersResource.searchByEmail(changeRequest.getEmail(), true);
            if (!users.isEmpty()) {
                UserRepresentation user = users.get(0);

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setTemporary(false);
                credential.setValue(changeRequest.getNewPassword());

                usersResource.get(user.getId()).resetPassword(credential);
                otpService.deleteKey(changeRequest.getEmail());
                return true;
            }
            ApiError apiError = new ApiError(
                    "Usuario no encontrado en el sistema.", // Mensaje claro
                    List.of(new ErrorField("email", "No existe un usuario con este correo."))
            );
            throw new CustomException(apiError, HttpStatus.NOT_FOUND);
        });
    }

    @Override
    public Boolean changePassword(String userId, String newPassword, boolean temporary) {
        return keycloakProvider.withRealm(realmResource -> {
            UserRepresentation user = realmResource.users().get(userId).toRepresentation();
            if (user != null) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(temporary);

                realmResource.users().get(userId).resetPassword(credential);
                return true;
            }
            throw new UserNotFoundException("User not found", new ErrorField("email/password", "Change Password not found"));
        });
    }

    @Override
    public Boolean firstChangePassword(String userId, String email, String newPassword, String oldPassword) {
        try {
            LoginRequest loginDTO = new LoginRequest(email, oldPassword);
            MultiValueMap<String, String> map = createAuthRequestMap(loginDTO.getUsername(), loginDTO.getPassword());
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, createHeaders());

            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakProvider.getTokenUri(),
                    HttpMethod.POST,
                    entity,
                    TokenResponse.class);

            // Si la autenticación es exitosa, significa que la contraseña antigua es válida
            // por lo que procedemos a cambiar la contraseña
            if (response.getStatusCode() == HttpStatus.OK) {
                return changePassword(userId, newPassword, false);
            }

            return false;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String errorResponse = e.getResponseBodyAsString();
                if (errorResponse.contains("invalid_grant")) {
                    // Si la contraseña antigua es inválida, podría ser el primer inicio de sesión
                    // con una contraseña temporal, entonces procedemos a cambiarla
                    changePassword(userId, newPassword, false);
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Boolean delete(String userId) {
        return keycloakProvider.withUsers(usersResource -> {
            try {
                UserRepresentation user = usersResource.get(userId).toRepresentation();
                if (user == null) {
                    throw new UserNotFoundException("User not found", new ErrorField("userId", "User not found with the provided ID"));
                }

                usersResource.get(userId).remove();

                return true;
            } catch (Exception e) {

                throw new RuntimeException("Failed to delete user", e);
            }
        });
    }

    @Override
    public void updateUser(String userId, UserRequest userRequest) {
        keycloakProvider.withUsers(usersResource -> {
            try {
                UserResource userResource = usersResource.get(userId);
                UserRepresentation user = userResource.toRepresentation();

                if (userRequest.getName() != null && !userRequest.getName().equals(user.getFirstName())) {
                    user.setFirstName(userRequest.getName());
                }
                if (userRequest.getLastName() != null && !userRequest.getLastName().equals(user.getLastName())) {
                    user.setLastName(userRequest.getLastName());
                }
                if (userRequest.getEmail() != null && !userRequest.getEmail().equals(user.getEmail())) {
                    user.setEmail(userRequest.getEmail());
                }

                userResource.update(user);
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to update user.", e);
            }
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> createAuthRequestMap(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakProvider.getClient_id());
        map.add("grant_type", keycloakProvider.getGrant_type());
        map.add("username", username);
        map.add("password", password);
        map.add("client_secret", keycloakProvider.getClient_secret());
        return map;
    }

    private TokenResponse handleAuthResponse(ResponseEntity<TokenResponse> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new AuthenticateNotFoundException("El usuario o contraseña es incorrecto. Por favor intente de nuevo.",
                    new ErrorField("correo/contraseña", "El usuario o contraseña es incorrecto. Por favor intente de nuevo."));
        }
    }

    private void handleAuthException(HttpClientErrorException e, String email, String name) {
    throw new AuthenticateNotFoundException(
            "El usuario o contraseña es incorrecto. Por favor intente de nuevo.",
            new ErrorField("email/password", "The username or password is incorrect. Please try again.")
    );
}

    private String createUser(String firstName, String lastName, String email, String username, String password, String role) {
        // CRITICAL FIX: Separar creación de usuario y asignación de roles para evitar deadlock
        // al adquirir múltiples conexiones del pool simultáneamente

        // Paso 1: Crear usuario y establecer password (usa una conexión del pool)
        String userId = keycloakProvider.withUsers(usersResource -> {
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setFirstName(firstName);
            userRepresentation.setLastName(lastName);
            userRepresentation.setEmail(email);
            userRepresentation.setUsername(username);
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(true);
            userRepresentation.setRequiredActions(new ArrayList<>());

            Response response = usersResource.create(userRepresentation);

            if (response.getStatus() == 201) {
                String newUserId = extractUserIdFromLocation(response.getLocation().getPath());
                setNewUserPassword(password, newUserId, usersResource);
                return newUserId;
            } else if (response.getStatus() == 409) {
                // Usuario ya existe en Keycloak (ej: creado por Google IDP)
                // Buscar el usuario existente y retornar su ID
                log.info("User already exists in Keycloak, searching for existing user with email: {}", email);
                List<UserRepresentation> existingUsers = usersResource.searchByEmail(email, true);
                if (!existingUsers.isEmpty()) {
                    String existingUserId = existingUsers.get(0).getId();
                    log.info("Found existing Keycloak user with ID: {}", existingUserId);
                    return existingUserId;
                }
                // Si no se encuentra por email, intentar por username
                existingUsers = usersResource.searchByUsername(username, true);
                if (!existingUsers.isEmpty()) {
                    String existingUserId = existingUsers.get(0).getId();
                    log.info("Found existing Keycloak user by username with ID: {}", existingUserId);
                    return existingUserId;
                }
                // Si aún no se encuentra, lanzar excepción
                throw new AlreadyExistsException("User already exists but could not be found", new ErrorField("email", "Email is already in use"));
            } else {
                throw new RuntimeException("Failed to create user");
            }
        });

        // Paso 2: Asignar roles (usa una conexión DIFERENTE del pool, liberada después del paso 1)
        List<String> roles = new ArrayList<>();
        roles.add(role);
        assignRolesToUser(roles, userId);

        return userId;
    }

    private String extractUserIdFromLocation(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void setNewUserPassword(String password, String userId, UsersResource usersResource) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    usersResource.get(userId).resetPassword(credential);

    UserRepresentation user = usersResource.get(userId).toRepresentation();
    user.setRequiredActions(new ArrayList<>());
    usersResource.get(userId).update(user);
}

    private void assignRolesToUser(List<String> roles, String userId) {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        keycloakProvider.withRealm(realmResource -> {
            try {
                UsersResource usersResource = realmResource.users();
                String clientId = realmResource.clients().findByClientId(keycloakProvider.getClient_id()).get(0).getId();
                ClientResource clientResource = realmResource.clients().get(clientId);
                List<RoleRepresentation> roleRepresentations = new ArrayList<>();

                RolesResource rolesResource = clientResource.roles();
                for (String roleName : roles) {
                    RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
                    if (role != null) {
                        roleRepresentations.add(role);
                    }
                }

                if (!roleRepresentations.isEmpty()) {
                    usersResource.get(userId).roles().clientLevel(clientId).add(roleRepresentations);
                }

                log.info("Roles {} assigned successfully to user {}", roles, userId);
                return null;
            } catch (Exception e) {
                log.error("Failed to assign roles {} to user {}: {}", roles, userId, e.getMessage(), e);
                throw new RuntimeException("Failed to assign roles to user: " + userId, e);
            }
        });
    }

    private void sendEmailOtp(String email, String name) {
        try {
            // Crear el objeto de solicitud
            SendMailJetEmailRequestDto requestDto = new SendMailJetEmailRequestDto();

            // Configurar destinatario
            List<MailJetRecipientDto> recipients = new ArrayList<>();
            recipients.add(new MailJetRecipientDto(email, name));
            requestDto.setRecipientEmail(recipients);

            // Configurar variables para la plantilla
            List<MailJetVarDto> vars = new ArrayList<>();
            String otp = otpService.generateOtpCode();
            otpService.saveOtpCode(email, otp);
            vars.add(new MailJetVarDto("otp_token", otp));

            requestDto.setMailJetVars(vars);

            // Configurar el asunto
            requestDto.setSubject("OTP de autenticación");

            // ID de la plantilla en Mailjet (este es un ejemplo, debe configurarse el ID correcto)
            requestDto.setTemplateId("5964805");

            // Enviar la solicitud
            cloudBridgesService.sendEmail(requestDto);

        } catch (Exception e) {
            // Manejar el error de envío de correo
            throw new RuntimeException("Error al enviar el correo de bienvenida: " + e.getMessage());
        }
    }

    /**
     * Genera un token JWT para un usuario específico usando Token Exchange de Keycloak.
     * Utiliza el service account del cliente para obtener un token en nombre del usuario.
     *
     * Flujo:
     * 1. Obtener token del service account usando client_credentials
     * 2. Realizar Token Exchange para obtener un token del usuario específico
     *
     * Requisitos en Keycloak:
     * - Feature token-exchange habilitado (--features=token-exchange)
     * - Cliente con Service Account habilitado
     * - Permisos de token-exchange configurados
     *
     * @param userId El ID de Keycloak del usuario (keycloakId)
     * @return TokenResponse con access_token y refresh_token del usuario
     */
    @Override
    public TokenResponse tokenExchangeForUser(String userId) {
        try {
            // Paso 1: Obtener token del service account
            String serviceAccountToken = getServiceAccountToken();

            // Paso 2: Realizar Token Exchange para el usuario
            return performTokenExchange(serviceAccountToken, userId);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String body = ex.getResponseBodyAsString();
            log.error("Token exchange failed for user {}: {}", userId, body);
            throw new CustomUnauthorizedException(
                    "Error en el intercambio de token.",
                    new ErrorField("tokenExchange", !body.isBlank() ? body : "Token exchange failed")
            );
        } catch (RestClientException ex) {
            log.error("Auth server not reachable during token exchange for user {}: {}", userId, ex.getMessage());
            throw new CustomUnauthorizedException(
                    "Servidor de autenticación no disponible.",
                    new ErrorField("auth", ex.getMessage())
            );
        }
    }

    /**
     * Obtiene un token para el service account del cliente usando client_credentials grant.
     */
    private String getServiceAccountToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", keycloakProvider.getClient_id());
        params.add("client_secret", keycloakProvider.getClient_secret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                keycloakProvider.getTokenUri(),
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new CustomUnauthorizedException(
                    "No se pudo obtener token del service account.",
                    new ErrorField("serviceAccount", "Failed to get service account token")
            );
        }

        return response.getBody().getAccessToken();
    }

    /**
     * Realiza el Token Exchange para obtener un token en nombre del usuario.
     */
    private TokenResponse performTokenExchange(String serviceAccountToken, String userId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        params.add("client_id", keycloakProvider.getClient_id());
        params.add("client_secret", keycloakProvider.getClient_secret());
        params.add("subject_token", serviceAccountToken);
        params.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
        params.add("requested_subject", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                keycloakProvider.getTokenUri(),
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new CustomUnauthorizedException(
                    "No se pudo realizar el intercambio de token.",
                    new ErrorField("tokenExchange", "Token exchange failed")
            );
        }

        log.info("Token exchange successful for user: {}", userId);
        return response.getBody();
    }
}
