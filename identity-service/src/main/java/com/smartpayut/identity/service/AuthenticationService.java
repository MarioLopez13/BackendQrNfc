package com.smartpayut.identity.service;

import com.smartpayut.identity.dto.request.LoginRequest;
import com.smartpayut.identity.dto.response.TokenResponse;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final KeycloakClient keycloak;

    public AuthenticationService(KeycloakClient keycloak) {
        this.keycloak = keycloak;
    }

    public TokenResponse authenticate(LoginRequest request) {
        return keycloak.authenticate(request.username().trim().toLowerCase(), request.password());
    }
}
