package com.smartpayut.identity.service.keycloak;

import com.smartpayut.identity.dto.request.UserRegistrationRequest;
import com.smartpayut.identity.dto.response.TokenResponse;
import com.smartpayut.identity.exception.IdentityException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.*;

@Component
public class KeycloakIdentityProvider implements KeycloakClient {
    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProvider.class);
    private final String serverUrl, realm, clientId, clientSecret, adminClientId, adminClientSecret;

    public KeycloakIdentityProvider(@Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm, @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret,
            @Value("${keycloak.admin-client-id}") String adminClientId,
            @Value("${keycloak.admin-client-secret}") String adminClientSecret) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminClientId = adminClientId;
        this.adminClientSecret = adminClientSecret;
    }

    private Keycloak client(String id, String secret) {
        return KeycloakBuilder.builder().serverUrl(serverUrl).realm(realm).grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(id).clientSecret(secret).build();
    }

    public TokenResponse authenticate(String username, String password) {
        try (Keycloak k = KeycloakBuilder.builder().serverUrl(serverUrl).realm(realm)
                .grantType(OAuth2Constants.PASSWORD).clientId(clientId).clientSecret(clientSecret).username(username)
                .password(password).build()) {
            AccessTokenResponse t = k.tokenManager().getAccessToken();
            return new TokenResponse(t.getToken(), t.getRefreshToken(), t.getTokenType(), t.getExpiresIn(),
                    subject(t.getToken()));
        } catch (Exception e) {
            throw new IdentityException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    public UUID createUser(UserRegistrationRequest r, UUID userAccountId) {
        try (Keycloak k = client(adminClientId, adminClientSecret)) {
            UserRepresentation u = new UserRepresentation();
            u.setUsername(r.userName());
            u.setEmail(r.email().toLowerCase());
            u.setFirstName(r.name());
            u.setLastName(r.lastName());
            u.setEnabled(true);
            u.setEmailVerified(false);
            u.setAttributes(Map.of("user_id", List.of(userAccountId.toString())));
            u.setCredentials(List.of(password(r.password())));
            try (Response response = k.realm(realm).users().create(u)) {
                if (response.getStatus() != 201)
                    throw new IdentityException(
                            response.getStatus() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_GATEWAY,
                            "Keycloak rechazó la creación del usuario");
                URI location = response.getLocation();
                UUID id = UUID.fromString(location.getPath().substring(location.getPath().lastIndexOf('/') + 1));
                try {
                    RoleRepresentation role = k.realm(realm).roles().get("USER").toRepresentation();
                    k.realm(realm).users().get(id.toString()).roles().realmLevel().add(List.of(role));
                    return id;
                } catch (Exception roleFailure) {
                    try (Response ignored = k.realm(realm).users().delete(id.toString())) {
                    }
                    throw roleFailure;
                }
            }
        } catch (IdentityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak createUser failed: {}", e.getMessage(), e);
            throw new IdentityException(HttpStatus.BAD_GATEWAY, "No fue posible crear el usuario en Keycloak");
        }
    }

    public void updateUser(UUID id, String email, String name, String lastName, Boolean enabled) {
        try (Keycloak k = client(adminClientId, adminClientSecret)) {
            var resource = k.realm(realm).users().get(id.toString());
            var u = resource.toRepresentation();
            if (email != null)
                u.setEmail(email);
            if (name != null)
                u.setFirstName(name);
            if (lastName != null)
                u.setLastName(lastName);
            if (enabled != null)
                u.setEnabled(enabled);
            resource.update(u);
        } catch (Exception e) {
            throw new IdentityException(HttpStatus.BAD_GATEWAY, "No fue posible actualizar el usuario en Keycloak");
        }
    }

    public void deleteUser(UUID id) {
        try (Keycloak k = client(adminClientId, adminClientSecret)) {
            Response r = k.realm(realm).users().delete(id.toString());
            try (r) {
                if (r.getStatus() != 204 && r.getStatus() != 404)
                    throw new IdentityException(HttpStatus.BAD_GATEWAY, "Keycloak rechazó la eliminación");
            }
        }
    }

    private static CredentialRepresentation password(String value) {
        var c = new CredentialRepresentation();
        c.setType(CredentialRepresentation.PASSWORD);
        c.setValue(value);
        c.setTemporary(false);
        return c;
    }

    private static String subject(String jwt) {
        try {
            return new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]))
                    .replaceAll(".*\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]+)\\\".*", "$1");
        } catch (Exception e) {
            return null;
        }
    }
}
