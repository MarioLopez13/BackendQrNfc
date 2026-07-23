package com.smartpayut.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.request.PaymentRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentExecutionServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private PaymentEventStateService eventStateService;

    private PaymentExecutionService service;

    @BeforeEach
    void setUp() {
        service = new PaymentExecutionService(
                paymentRepository,
                walletClient,
                eventStateService,
                new PaymentMapper());
    }

    @Test
    void executesQrDebitAndPublishesCompletedEvent() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        when(paymentRepository.findByIdempotencyKey("qr-001")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletClient.getCurrentWallet("Bearer user-token"))
                .thenReturn(new WalletResponse(walletId, userId, new BigDecimal("10.00"), "USD", "ACTIVE"));
        when(walletClient.debit(any())).thenReturn(new WalletMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("0.35"),
                new BigDecimal("10.00"),
                new BigDecimal("9.65"),
                "reference"));
        when(eventStateService.complete(
                any(Payment.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("payment.completed")))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.complete(invocation.getArgument(1), invocation.getArgument(2));
                    return payment;
                });

        PaymentResponse response = service.execute(
                new PaymentRequest("BUS-12", "Ruta Central", new BigDecimal("0.35")),
                PaymentMethod.QR,
                "qr-001",
                "Bearer user-token");

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.updatedBalance()).isEqualByComparingTo("9.65");
        verify(walletClient).debit(any());
        verify(eventStateService).complete(
                any(Payment.class),
                eq(new BigDecimal("10.00")),
                eq(new BigDecimal("9.65")),
                eq("payment.completed"));
    }

    @Test
    void executesNfcDebitAndRegistersCompletedEvent() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        when(paymentRepository.findByIdempotencyKey("nfc-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletClient.getCurrentWallet("Bearer user-token"))
                .thenReturn(new WalletResponse(walletId, userId, new BigDecimal("5.00"), "USD", "ACTIVE"));
        when(walletClient.debit(any())).thenReturn(new WalletMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("0.35"),
                new BigDecimal("5.00"),
                new BigDecimal("4.65"),
                "reference"));
        when(eventStateService.complete(
                any(Payment.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("payment.completed")))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.complete(invocation.getArgument(1), invocation.getArgument(2));
                    return payment;
                });

        PaymentResponse response = service.execute(
                new PaymentRequest("BUS-15", "Ruta Norte", new BigDecimal("0.35")),
                PaymentMethod.NFC,
                "nfc-new",
                "Bearer user-token");

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventStateService).complete(
                any(Payment.class),
                eq(new BigDecimal("5.00")),
                eq(new BigDecimal("4.65")),
                eq("payment.completed"));
    }

    @Test
    void returnsExistingPaymentWithoutSecondDebit() {
        UUID userId = UUID.randomUUID();
        Payment existing = new Payment(
                userId,
                UUID.randomUUID(),
                PaymentMethod.NFC,
                new BigDecimal("0.35"),
                "nfc-001",
                "BUS-15",
                "Ruta Norte");
        existing.complete(new BigDecimal("4.00"), new BigDecimal("3.65"));
        when(paymentRepository.findByIdempotencyKey("nfc-001")).thenReturn(Optional.of(existing));

        PaymentResponse response = service.execute(
                new PaymentRequest("BUS-15", "Ruta Norte", new BigDecimal("0.35")),
                PaymentMethod.NFC,
                "nfc-001",
                "Bearer user-token");

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        org.mockito.Mockito.verifyNoInteractions(walletClient);
    }
}
