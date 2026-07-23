package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.domain.enumeration.UserStatus;
import com.smartpayut.identity.dto.request.*;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import com.smartpayut.identity.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    UserAccountRepository accounts = mock(UserAccountRepository.class);
    UserProfileRepository profiles = mock(UserProfileRepository.class);
    KeycloakClient keycloak = mock(KeycloakClient.class);
    UserService service = new UserService(accounts, profiles, keycloak, new UserAccountMapper());

    UserAccount user() {
        return new UserAccount(UUID.randomUUID(), UUID.randomUUID(), "mario@test.com", "mario@test.com", "Mario",
                "Lopez");
    }

    @Test
    void actualizacionParcial() {
        var u = user();
        when(accounts.findById(u.getId())).thenReturn(Optional.of(u));
        when(profiles.findByUserAccountId(u.getId())).thenReturn(Optional.empty());
        var result = service.update(u.getId(),
                new UserUpdateRequest("José", null, null, UserStatus.INACTIVE, null, null, null));
        assertEquals("José", result.name());
        assertEquals(UserStatus.INACTIVE, result.status());
        verify(keycloak).updateUser(u.getKeycloakId(), null, "José", null, false);
    }

    @Test
    void eliminacionCuentaValidaCredenciales() {
        var u = user();
        when(accounts.findByEmailIgnoreCase(u.getEmail())).thenReturn(Optional.of(u));
        service.delete(new DeleteAccountRequest(u.getEmail(), "secret"), u.getKeycloakId().toString());
        verify(keycloak).authenticate(u.getEmail(), "secret");
        verify(keycloak).deleteUser(u.getKeycloakId());
        assertEquals(UserStatus.DELETED, u.getStatus());
    }
}
