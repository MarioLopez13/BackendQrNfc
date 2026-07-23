package com.smartpayut.identity.service;

import com.smartpayut.identity.dto.request.LoginRequest;
import com.smartpayut.identity.dto.response.TokenResponse;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    KeycloakClient keycloak = mock(KeycloakClient.class);
    AuthenticationService service = new AuthenticationService(keycloak);

    @Test
    void loginExitoso() {
        var expected = new TokenResponse("access", "refresh", "Bearer", 300, "id");
        when(keycloak.authenticate("user@test.com", "secret")).thenReturn(expected);
        assertSame(expected, service.authenticate(new LoginRequest("USER@test.com", "secret")));
    }

    @Test
    void loginInvalido() {
        when(keycloak.authenticate(anyString(), anyString()))
                .thenThrow(new IdentityException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        assertThrows(IdentityException.class, () -> service.authenticate(new LoginRequest("bad", "bad")));
    }
}
