package com.smartpayut.identity.service;

import com.smartpayut.identity.domain.entity.*;
import com.smartpayut.identity.dto.request.UserRegistrationRequest;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.*;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import com.smartpayut.identity.service.keycloak.KeycloakClient;
import com.smartpayut.identity.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserRegistrationServiceTest {
    UserAccountRepository accounts = mock(UserAccountRepository.class);
    UserProfileRepository profiles = mock(UserProfileRepository.class);
    KeycloakClient keycloak = mock(KeycloakClient.class);
    IdentityEventPublisher events = mock(IdentityEventPublisher.class);
    UserRegistrationService service = new UserRegistrationService(accounts, profiles, keycloak, events,
            new UserAccountMapper(), new UserProfileMapper());
    UserRegistrationRequest request = new UserRegistrationRequest("user@test.com", "user@test.com", "Mario", "Lopez",
            "password1");

    @BeforeEach
    void setup() {
        when(keycloak.createUser(any(), any())).thenReturn(UUID.randomUUID());
        when(accounts.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(profiles.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registroExitosoPublicaEvento() {
        var response = service.register(request);
        assertEquals("user@test.com", response.email());
        verify(events).publish(argThat(e -> e.eventType().equals("identity.user.created") && e.eventVersion() == 1));
    }

    @Test
    void emailDuplicado() {
        when(accounts.existsByEmailIgnoreCase(anyString())).thenReturn(true);
        assertThrows(IdentityException.class, () -> service.register(request));
        verifyNoInteractions(events);
    }

    @Test
    void usernameDuplicado() {
        when(accounts.existsByUserNameIgnoreCase(anyString())).thenReturn(true);
        assertThrows(IdentityException.class, () -> service.register(request));
        verifyNoInteractions(events);
    }

    @Test
    void falloCreacionKeycloak() {
        when(keycloak.createUser(any(), any()))
                .thenThrow(new IdentityException(org.springframework.http.HttpStatus.BAD_GATEWAY, "error"));
        assertThrows(IdentityException.class, () -> service.register(request));
        verify(accounts, never()).saveAndFlush(any());
    }

    @Test
    void falloPersistenciaCompensaKeycloak() {
        UUID id = UUID.randomUUID();
        when(keycloak.createUser(any(), any())).thenReturn(id);
        when(accounts.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("db"));
        assertThrows(IdentityException.class, () -> service.register(request));
        verify(keycloak).deleteUser(id);
    }
}
