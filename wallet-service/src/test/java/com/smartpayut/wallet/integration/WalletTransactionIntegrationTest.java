package com.smartpayut.wallet.integration;

import com.smartpayut.wallet.domain.entity.Wallet;
import com.smartpayut.wallet.dto.request.BalanceOperationRequest;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import com.smartpayut.wallet.repository.*;
import com.smartpayut.wallet.service.WalletMovementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class WalletTransactionIntegrationTest {
    @Autowired
    WalletMovementService service;
    @Autowired
    WalletRepository wallets;
    @Autowired
    WalletMovementRepository movements;
    @MockBean
    WalletEventPublisher publisher;
    Wallet wallet;

    @BeforeEach
    void setup() {
        movements.deleteAll();
        wallets.deleteAll();
        wallet = new Wallet(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        wallet.changeBalance(new BigDecimal("10.00"));
        wallet = wallets.saveAndFlush(wallet);
    }

    @Test
    void falloRabbitRevierteWalletYMovimiento() {
        doThrow(new WalletException(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible")).when(publisher)
                .publish(any());
        var request = new BalanceOperationRequest(wallet.getUserId(), new BigDecimal("2.00"), "rollback-key", null,
                "test", null);
        assertThrows(WalletException.class, () -> service.credit(request));
        assertEquals(new BigDecimal("10.00"), wallets.findById(wallet.getId()).orElseThrow().getBalance());
        assertTrue(movements.findByIdempotencyKey("rollback-key").isEmpty());
    }
}
