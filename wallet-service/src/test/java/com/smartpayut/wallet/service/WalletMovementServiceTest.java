package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.*;
import com.smartpayut.wallet.domain.enumeration.*;
import com.smartpayut.wallet.dto.request.BalanceOperationRequest;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.mapper.WalletMovementMapper;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import com.smartpayut.wallet.repository.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WalletMovementServiceTest {
    WalletRepository wallets = mock(WalletRepository.class);
    WalletMovementRepository movements = mock(WalletMovementRepository.class);
    WalletEventPublisher events = mock(WalletEventPublisher.class);
    WalletMovementService service = new WalletMovementService(wallets, movements, new WalletMovementMapper(), events);
    Wallet wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    BalanceOperationRequest req(String key, BigDecimal amount) {
        return new BalanceOperationRequest(wallet.getUserId(), amount, key, "pay-1", "test", null);
    }

    @BeforeEach
    void setup() {
        when(wallets.findByUserIdForUpdate(wallet.getUserId())).thenReturn(Optional.of(wallet));
        when(movements.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(wallets.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void creditoExitosoCreaMovimientoYEvento() {
        var r = service.credit(req("c1", new BigDecimal("10.00")));
        assertEquals(new BigDecimal("10.00"), r.balanceAfter());
        verify(movements).saveAndFlush(any());
        verify(events).publish(argThat(e -> e.eventType().equals("wallet.credited")));
    }

    @Test
    void debitoExitoso() {
        wallet.changeBalance(new BigDecimal("10.00"));
        var r = service.debit(req("d1", new BigDecimal("3.00")));
        assertEquals(new BigDecimal("7.00"), r.balanceAfter());
    }

    @Test
    void saldoInsuficienteImpideNegativo() {
        wallet.changeBalance(new BigDecimal("1.00"));
        assertThrows(WalletException.class, () -> service.debit(req("d2", new BigDecimal("2.00"))));
        verify(movements, never()).saveAndFlush(any());
    }

    @Test
    void montoCero() {
        assertThrows(WalletException.class, () -> service.credit(req("z", BigDecimal.ZERO)));
    }

    @Test
    void montoNegativo() {
        assertThrows(WalletException.class, () -> service.credit(req("n", new BigDecimal("-1"))));
    }

    @Test
    void creditoIdempotente() {
        WalletMovement old = new WalletMovement(UUID.randomUUID(), wallet, MovementType.ADJUSTMENT_CREDIT,
                new BigDecimal("2"), BigDecimal.ZERO, new BigDecimal("2"), null, "same", null);
        when(movements.findByIdempotencyKey("same")).thenReturn(Optional.of(old));
        service.credit(req("same", new BigDecimal("2")));
        verify(wallets, never()).findByUserIdForUpdate(any());
    }

    @Test
    void debitoIdempotente() {
        WalletMovement old = new WalletMovement(UUID.randomUUID(), wallet, MovementType.PAYMENT_DEBIT,
                new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("3"), "pay-1", "same-d", null);
        when(movements.findByIdempotencyKey("same-d")).thenReturn(Optional.of(old));
        service.debit(req("same-d", new BigDecimal("2")));
        verify(wallets, never()).findByUserIdForUpdate(any());
    }

    @Test
    void reembolsoExitoso() {
        when(movements.existsByReferenceIdAndType("pay-1", MovementType.PAYMENT_DEBIT)).thenReturn(true);
        var r = service.refund(req("r1", new BigDecimal("2")));
        assertEquals(MovementType.PAYMENT_REFUND, r.type());
    }

    @Test
    void reembolsoDuplicado() {
        WalletMovement old = new WalletMovement(UUID.randomUUID(), wallet, MovementType.PAYMENT_REFUND,
                new BigDecimal("2"), BigDecimal.ZERO, new BigDecimal("2"), "pay-1", "r-old", null);
        when(movements.existsByReferenceIdAndType("pay-1", MovementType.PAYMENT_DEBIT)).thenReturn(true);
        when(movements.findByIdempotencyKey("r-old")).thenReturn(Optional.of(old));
        service.refund(req("r-old", new BigDecimal("2")));
        verify(wallets, never()).findByUserIdForUpdate(any());
    }
}
