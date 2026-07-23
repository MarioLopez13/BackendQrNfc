package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.*;
import com.smartpayut.wallet.event.*;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import com.smartpayut.wallet.repository.*;
import org.junit.jupiter.api.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {
    WalletRepository wallets = mock(WalletRepository.class);
    ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
    WalletEventPublisher events = mock(WalletEventPublisher.class);
    WalletService service = new WalletService(wallets, processed, events);

    IdentityUserCreatedEvent event() {
        return new IdentityUserCreatedEvent(UUID.randomUUID(), "identity.user.created", 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), "u", "e@x.com", "N", "L", "ACTIVE");
    }

    @BeforeEach
    void setup() {
        when(wallets.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(processed.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void creaWalletConSaldoCero() {
        var e = event();
        assertTrue(service.createFrom(e));
        verify(wallets).saveAndFlush(argThat(w -> w.getUserId().equals(e.userId())
                && w.getKeycloakId().equals(e.keycloakId()) && w.getBalance().toPlainString().equals("0.00")));
        verify(events).publish(argThat(x -> x.eventType().equals("wallet.created")));
    }

    @Test
    void eventoDuplicadoNoCrea() {
        var e = event();
        when(processed.existsById(e.eventId())).thenReturn(true);
        assertFalse(service.createFrom(e));
        verify(wallets, never()).save(any());
    }

    @Test
    void userIdDuplicadoNoCrea() {
        var e = event();
        when(wallets.existsByUserId(e.userId())).thenReturn(true);
        assertFalse(service.createFrom(e));
        verify(processed).save(any());
    }

    @Test
    void keycloakIdDuplicadoNoCrea() {
        var e = event();
        when(wallets.existsByKeycloakId(e.keycloakId())).thenReturn(true);
        assertFalse(service.createFrom(e));
        verify(processed).save(any());
    }
}
