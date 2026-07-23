package com.smartpayut.identity.service.keycloak;

import com.smartpayut.identity.dto.request.UserRegistrationRequest;
import com.smartpayut.identity.dto.response.TokenResponse;
import java.util.UUID;

public interface KeycloakClient {
    TokenResponse authenticate(String username, String password);

    UUID createUser(UserRegistrationRequest request, UUID userAccountId);

    void updateUser(UUID keycloakId, String email, String name, String lastName, Boolean enabled);

    void deleteUser(UUID keycloakId);
}
