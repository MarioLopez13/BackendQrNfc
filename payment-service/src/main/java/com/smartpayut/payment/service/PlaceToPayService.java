package com.smartpayut.payment.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartpayut.payment.client.placetopay.PlaceToPayClient;
import com.smartpayut.payment.client.placetopay.PlaceToPayClient.SessionCommand;
import com.smartpayut.payment.client.placetopay.PlaceToPayClient.SessionResult;
import com.smartpayut.payment.client.placetopay.PlaceToPayClient.SessionStatus;
import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.request.PlaceToPayCallbackRequest;
import com.smartpayut.payment.dto.request.PlaceToPayTopUpRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.TopUpResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.exception.PaymentNotFoundException;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.repository.PaymentRepository;
import com.smartpayut.payment.util.IdempotencyKeys;

@Service
public class PlaceToPayService {

    private final PaymentRepository paymentRepository;
    private final WalletClient walletClient;
    private final PlaceToPayClient placeToPayClient;
    private final PaymentEventStateService eventStateService;
    private final PaymentMapper mapper;
    private final int defaultExpirationMinutes;

    public PlaceToPayService(
            PaymentRepository paymentRepository,
            WalletClient walletClient,
            PlaceToPayClient placeToPayClient,
            PaymentEventStateService eventStateService,
            PaymentMapper mapper,
            @Value("${payment.placetopay.default-expiration-minutes}") int defaultExpirationMinutes) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
        this.placeToPayClient = placeToPayClient;
        this.eventStateService = eventStateService;
        this.mapper = mapper;
        this.defaultExpirationMinutes = defaultExpirationMinutes;
    }

    public TopUpResponse create(
            PlaceToPayTopUpRequest request,
            String suppliedIdempotencyKey,
            String bearerToken,
            String ipAddress,
            String userAgent) {
        String key = IdempotencyKeys.resolve(suppliedIdempotencyKey, "topup");
        return paymentRepository.findByIdempotencyKey(key)
                .map(mapper::toTopUpResponse)
                .orElseGet(() -> createNew(request, key, bearerToken, ipAddress, userAgent));
    }

    public PaymentResponse confirm(UUID topUpId) {
        Payment payment = required(topUpId);
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return mapper.toResponse(payment);
        }
        SessionStatus status = placeToPayClient.querySession(payment.getPlaceToPayRequestId());
        applyStatus(payment, status.status(), status.message());
        return mapper.toResponse(payment);
    }

    public void callback(PlaceToPayCallbackRequest request) {
        if (!placeToPayClient.isValidCallback(
                request.requestId(), request.status().status(), request.status().date(), request.signature())) {
            throw new IllegalArgumentException("Firma de callback PlaceToPay invÃ¡lida.");
        }
        Payment payment = paymentRepository.findByPlaceToPayRequestId(request.requestId())
                .orElseThrow(() -> new PaymentNotFoundException("Recarga PlaceToPay no encontrada."));
        applyStatus(payment, request.status().status(), request.status().message());
    }

    private TopUpResponse createNew(
            PlaceToPayTopUpRequest request,
            String key,
            String bearerToken,
            String ipAddress,
            String userAgent) {
        WalletResponse wallet = walletClient.getCurrentWallet(bearerToken);
        Payment payment = paymentRepository.save(new Payment(
                wallet.userId(),
                wallet.id(),
                PaymentMethod.PLACETOPAY,
                request.amount(),
                key,
                "WALLET",
                "Recarga de saldo"));
        String reference = "TOPUP-" + payment.getId();
        int expiration = request.expirationMinutes() == null
                ? defaultExpirationMinutes : request.expirationMinutes();
        SessionResult session = placeToPayClient.createSession(new SessionCommand(
                reference,
                "Recarga SmartPayUT",
                request.amount(),
                expiration,
                request.returnUrl(),
                request.cancelUrl(),
                ipAddress,
                userAgent));
        payment.assignPlaceToPaySession(session.requestId(), session.processUrl(), reference);
        paymentRepository.save(payment);
        return mapper.toTopUpResponse(payment);
    }

    private void applyStatus(Payment payment, String providerStatus, String providerMessage) {
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }
        if ("APPROVED".equalsIgnoreCase(providerStatus)) {
            credit(payment);
        } else if ("REJECTED".equalsIgnoreCase(providerStatus)
                || "FAILED".equalsIgnoreCase(providerStatus)) {
            eventStateService.fail(payment, providerMessage, "topup.failed");
        }
    }

    private void credit(Payment payment) {
        WalletMovementResponse movement = walletClient.credit(new WalletMovementRequest(
                payment.getUserAccountId(),
                payment.getAmount(),
                "topup:" + payment.getId() + ":credit",
                payment.getId().toString(),
                "Recarga PlaceToPay",
                "TOP_UP"));
        eventStateService.complete(
                payment,
                movement.balanceBefore(),
                movement.balanceAfter(),
                "topup.completed");
    }

    private Payment required(UUID topUpId) {
        Payment payment = paymentRepository.findById(topUpId)
                .orElseThrow(() -> new PaymentNotFoundException("Recarga no encontrada."));
        if (payment.getMethod() != PaymentMethod.PLACETOPAY) {
            throw new PaymentNotFoundException("Recarga no encontrada.");
        }
        return payment;
    }
}
