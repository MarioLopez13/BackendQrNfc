package com.kynsof.identity.infrastructure.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class KeycloakProvider {

    @Value("${keycloak.provider.server-url}")
    private String server_url;

    @Value("${keycloak.provider.realm-name}")
    private String realm_name;

    @Value("${keycloak.provider.realm-master}")
    private String realm_master;

    @Value("${keycloak.provider.admin-clic}")
    private String admin_clic;

    @Value("${keycloak.provider.user-console}")
    private String user_console;

    @Value("${keycloak.provider.password-console}")
    private String password_console;

    @Value("${keycloak.provider.client-secret}")
    private String client_secret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.provider.client-id}")
    private String client_id;

    @Value("${keycloak.provider.grant-type}")
    private String grant_type;

    private Keycloak createKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(server_url)
                .realm(realm_master)
                .clientId(admin_clic)
                .username(user_console)
                .password(password_console)
                .clientSecret(client_secret)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }

    public RealmResource getRealmResource() {
        Keycloak keycloak = createKeycloakClient();
        return keycloak.realm(realm_name);
    }

    public UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }

    /**
     * Ejecuta una operación con el RealmResource y cierra la conexión después.
     */
    public <T> T withRealm(RealmOperation<T> operation) {
        Keycloak keycloak = null;
        try {
            keycloak = createKeycloakClient();
            RealmResource realm = keycloak.realm(realm_name);
            return operation.execute(realm);
        } finally {
            if (keycloak != null) {
                try {
                    keycloak.close();
                } catch (Exception e) {
                    log.warn("Error closing Keycloak connection: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Ejecuta una operación con el UsersResource y cierra la conexión después.
     */
    public <T> T withUsers(UsersOperation<T> operation) {
        Keycloak keycloak = null;
        try {
            keycloak = createKeycloakClient();
            UsersResource users = keycloak.realm(realm_name).users();
            return operation.execute(users);
        } finally {
            if (keycloak != null) {
                try {
                    keycloak.close();
                } catch (Exception e) {
                    log.warn("Error closing Keycloak connection: {}", e.getMessage());
                }
            }
        }
    }

    @FunctionalInterface
    public interface RealmOperation<T> {
        T execute(RealmResource realm);
    }

    @FunctionalInterface
    public interface UsersOperation<T> {
        T execute(UsersResource users);
    }
}