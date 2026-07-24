package com.smartpayut.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.request.PlaceToPayTopUpRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.TopUpResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.exception.ExternalServiceException;
import com.smartpayut.payment.exception.PaymentConflictException;
import com.smartpayut.payment.exception.PaymentNotFoundException;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PlaceToPayServiceTest {

    private static final String BEARER_TOKEN = "Bearer token";
    private static final String SIMULATION_URL = "http://localhost:5174/#/top-up/simulated";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private PlaceToPayClient placeToPayClient;

    @Mock
    private PaymentEventStateService eventStateService;

    private UUID walletId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void realModeCreatesSandboxSessionWithoutCreditingWallet() {
        PlaceToPayService service = service(false);
        prepareNewPayment();
        when(placeToPayClient.createSession(any())).thenReturn(
                new PlaceToPayClient.SessionResult(7788L, "https://sandbox/session/7788", "OK", "Created"));

        TopUpResponse response = service.create(request(), "topup-001", BEARER_TOKEN, "127.0.0.1", "JUnit");

        assertThat(response.requestId()).isEqualTo(7788L);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(response.processUrl()).isEqualTo("https://sandbox/session/7788");
        verify(placeToPayClient).createSession(any());
        verify(walletClient, never()).credit(any());
    }

    @Test
    void simulationCreatesCompatiblePendingSessionWithoutCallingPlaceToPay() {
        PlaceToPayService service = service(true);
        prepareNewPayment();

        TopUpResponse response = service.create(request(), "topup-001", BEARER_TOKEN, "127.0.0.1", "JUnit");

        assertThat(response.topUpId()).isNotNull();
        assertThat(response.requestId()).isPositive();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(response.status()).isEqualTo("PENDING_PAY");
        assertThat(response.paymentType()).isEqualTo("PLACETOPAY");
        assertThat(response.processUrl()).isEqualTo(SIMULATION_URL + "/" + response.topUpId());
        verify(placeToPayClient, never()).createSession(any());
        verify(walletClient, never()).credit(any());
    }

    @Test
    void simulationConfirmationCreditsWalletAndCompletesThroughEventStateService() {
        PlaceToPayService service = service(true);
        Payment payment = pendingPayment(userId);
        prepareConfirmation(payment, userId);
        when(walletClient.credit(any(WalletMovementRequest.class))).thenReturn(movement());
        completePaymentWhenEventStateIsCalled();

        PaymentResponse response = service.confirm(payment.getId(), BEARER_TOKEN);

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.previousBalance()).isEqualByComparingTo("0.00");
        assertThat(response.updatedBalance()).isEqualByComparingTo("3.00");
        verify(walletClient).credit(any(WalletMovementRequest.class));
        verify(eventStateService).complete(payment, BigDecimal.ZERO, new BigDecimal("3.00"), "topup.completed");
        verify(placeToPayClient, never()).querySession(anyLong());
    }

    @Test
    void repeatedConfirmationReturnsCompletedPaymentWithoutSecondCredit() {
        PlaceToPayService service = service(true);
        Payment payment = pendingPayment(userId);
        payment.complete(BigDecimal.ZERO, new BigDecimal("3.00"));
        prepareConfirmation(payment, userId);

        PaymentResponse response = service.confirm(payment.getId(), BEARER_TOKEN);

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(walletClient, never()).credit(any());
        verify(eventStateService, never()).complete(any(), any(), any(), any());
    }

    @Test
    void walletFailureLeavesPaymentRecoverableAndDoesNotCreateCompletedEvent() {
        PlaceToPayService service = service(true);
        Payment payment = pendingPayment(userId);
        prepareConfirmation(payment, userId);
        when(walletClient.credit(any(WalletMovementRequest.class)))
                .thenThrow(new ExternalServiceException("Wallet no disponible."));

        assertThatThrownBy(() -> service.confirm(payment.getId(), BEARER_TOKEN))
                .isInstanceOf(ExternalServiceException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        verify(eventStateService, never()).complete(any(), any(), any(), eq("topup.completed"));
    }

    @Test
    void foreignUserCannotConfirmTopUp() {
        PlaceToPayService service = service(true);
        Payment payment = pendingPayment(UUID.randomUUID());
        prepareConfirmation(payment, userId);

        assertThatThrownBy(() -> service.confirm(payment.getId(), BEARER_TOKEN))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(walletClient, never()).credit(any());
    }

    @Test
    void missingTopUpReturnsNotFound() {
        PlaceToPayService service = service(true);
        UUID topUpId = UUID.randomUUID();
        when(walletClient.getCurrentWallet(BEARER_TOKEN)).thenReturn(wallet());
        when(paymentRepository.findByIdForUpdate(topUpId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(topUpId, BEARER_TOKEN))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void failedTopUpCannotBeConfirmed() {
        PlaceToPayService service = service(true);
        Payment payment = pendingPayment(userId);
        payment.fail("Rejected");
        prepareConfirmation(payment, userId);

        assertThatThrownBy(() -> service.confirm(payment.getId(), BEARER_TOKEN))
                .isInstanceOf(PaymentConflictException.class);

        verify(walletClient, never()).credit(any());
    }

    private PlaceToPayService service(boolean simulationEnabled) {
        return new PlaceToPayService(
                paymentRepository,
                walletClient,
                placeToPayClient,
                eventStateService,
                new PaymentMapper(),
                30,
                simulationEnabled,
                SIMULATION_URL);
    }

    private void prepareNewPayment() {
        when(paymentRepository.findByIdempotencyKey("topup-001")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletClient.getCurrentWallet(BEARER_TOKEN)).thenReturn(wallet());
    }

    private void prepareConfirmation(Payment payment, UUID currentUserId) {
        when(walletClient.getCurrentWallet(BEARER_TOKEN)).thenReturn(
                new WalletResponse(walletId, currentUserId, BigDecimal.ZERO, "USD", "ACTIVE"));
        when(paymentRepository.findByIdForUpdate(payment.getId())).thenReturn(Optional.of(payment));
    }

    private void completePaymentWhenEventStateIsCalled() {
        when(eventStateService.complete(any(Payment.class), any(), any(), eq("topup.completed")))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    BigDecimal balanceBefore = invocation.getArgument(1);
                    BigDecimal balanceAfter = invocation.getArgument(2);
                    payment.complete(balanceBefore, balanceAfter);
                    return payment;
                });
    }

    private PlaceToPayTopUpRequest request() {
        return new PlaceToPayTopUpRequest(
                new BigDecimal("3.00"),
                30,
                "P2P_SMARTPAYUT_TEST",
                "http://localhost:5174/result",
                "http://localhost:5174/cancelled");
    }

    private WalletResponse wallet() {
        return new WalletResponse(walletId, userId, BigDecimal.ZERO, "USD", "ACTIVE");
    }

    private WalletMovementResponse movement() {
        return new WalletMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("3.00"),
                BigDecimal.ZERO,
                new BigDecimal("3.00"),
                UUID.randomUUID().toString());
    }

    private Payment pendingPayment(UUID ownerId) {
        Payment payment = new Payment(
                ownerId,
                walletId,
                PaymentMethod.PLACETOPAY,
                new BigDecimal("3.00"),
                "topup-" + UUID.randomUUID(),
                "WALLET",
                "Recarga de saldo");
        payment.assignPlaceToPaySession(12345L, SIMULATION_URL + "/" + payment.getId(), "TOPUP-" + payment.getId());
        return payment;
    }
}
