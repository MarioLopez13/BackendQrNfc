package com.smartpayut.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;

import com.smartpayut.identity.domain.entity.UserAccount;
import com.smartpayut.identity.event.IdentityUserCreatedEvent;
import com.smartpayut.identity.exception.IdentityException;
import com.smartpayut.identity.mapper.UserAccountMapper;
import com.smartpayut.identity.mapper.UserProfileMapper;
import com.smartpayut.identity.messaging.publisher.IdentityEventPublisher;
import com.smartpayut.identity.repository.UserAccountRepository;
import com.smartpayut.identity.repository.UserProfileRepository;

class UserReconciliationServiceTest {

    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final IdentityEventPublisher events = mock(IdentityEventPublisher.class);
    private final UserReconciliationService service = new UserReconciliationService(
            accounts,
            profiles,
            events,
            new UserAccountMapper(),
            new UserProfileMapper());

    @BeforeEach
    void setup() {
        when(accounts.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsLocalAccountAndPublishesOfficialEvent() {
        UUID keycloakId = UUID.randomUUID();

        var response = service.reconcile(jwt(keycloakId));
        ArgumentCaptor<IdentityUserCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(IdentityUserCreatedEvent.class);

        assertEquals("user@kynsoft.com", response.email());
        verify(events).publish(eventCaptor.capture());
        assertEquals(keycloakId, eventCaptor.getValue().keycloakId());
        assertEquals(response.id(), eventCaptor.getValue().userId());
    }

    @Test
    void returnsExistingAccountWithoutPublishingAnotherEvent() {
        UUID keycloakId = UUID.randomUUID();
        UserAccount existing = new UserAccount(
                UUID.randomUUID(),
                keycloakId,
                "user@kynsoft.com",
                "user@kynsoft.com",
                "User",
                "Kynsoft");
        when(accounts.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existing));

        var response = service.reconcile(jwt(keycloakId));

        assertEquals(existing.getId(), response.id());
        verify(events, never()).publish(any());
    }

    @Test
    void rejectsEmailOwnedByAnotherLocalAccount() {
        UUID keycloakId = UUID.randomUUID();
        UserAccount other = new UserAccount(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "other",
                "user@kynsoft.com",
                "Other",
                "User");
        when(accounts.findByEmailIgnoreCase("user@kynsoft.com")).thenReturn(Optional.of(other));

        assertThrows(IdentityException.class, () -> service.reconcile(jwt(keycloakId)));
        verify(accounts, never()).saveAndFlush(any());
        verify(events, never()).publish(any());
    }

    private Jwt jwt(UUID subject) {
        return new Jwt(
                "token",
                null,
                null,
                Map.of("alg", "none"),
                Map.of(
                        "sub", subject.toString(),
                        "preferred_username", "user@kynsoft.com",
                        "email", "user@kynsoft.com",
                        "given_name", "User",
                        "family_name", "Kynsoft"));
    }
}
