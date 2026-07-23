package com.smartpayut.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.smartpayut.payment.client.placetopay.PlaceToPayClient;
import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.request.PlaceToPayTopUpRequest;
import com.smartpayut.payment.dto.response.TopUpResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.messaging.publisher.PaymentEventPublisher;
import com.smartpayut.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PlaceToPayServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private PlaceToPayClient placeToPayClient;

    @Mock
    private PaymentEventPublisher eventPublisher;

    private PlaceToPayService service;

    @BeforeEach
    void setUp() {
        service = new PlaceToPayService(
                paymentRepository,
                walletClient,
                placeToPayClient,
                eventPublisher,
                new PaymentMapper(),
                30);
    }

    @Test
    void createsSandboxSessionWithoutCreditingWallet() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(paymentRepository.findByIdempotencyKey("topup-001")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletClient.getCurrentWallet("Bearer token"))
                .thenReturn(new WalletResponse(walletId, userId, BigDecimal.ZERO, "USD", "ACTIVE"));
        when(placeToPayClient.createSession(any())).thenReturn(
                new PlaceToPayClient.SessionResult(7788L, "https://sandbox/session/7788", "OK", "Created"));

        TopUpResponse response = service.create(
                new PlaceToPayTopUpRequest(
                        new BigDecimal("20.00"),
                        30,
                        "P2P_SMARTPAYUT_TEST",
                        "https://smartpayut.app/result",
                        "https://smartpayut.app/cancelled"),
                "topup-001",
                "Bearer token",
                "127.0.0.1",
                "JUnit");

        assertThat(response.requestId()).isEqualTo(7788L);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
        org.mockito.Mockito.verify(walletClient, org.mockito.Mockito.never()).credit(any());
    }
}
