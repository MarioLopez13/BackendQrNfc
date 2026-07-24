package com.smartpayut.payment.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.smartpayut.payment.exception.PaymentConflictException;
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
    private final boolean simulationEnabled;
    private final String simulationProcessUrlBase;

    public PlaceToPayService(
            PaymentRepository paymentRepository,
            WalletClient walletClient,
            PlaceToPayClient placeToPayClient,
            PaymentEventStateService eventStateService,
            PaymentMapper mapper,
            @Value("${payment.placetopay.default-expiration-minutes}") int defaultExpirationMinutes,
            @Value("${payment.placetopay.simulation-enabled:false}") boolean simulationEnabled,
            @Value("${payment.placetopay.simulation-process-url-base:"
                    + "http://localhost:5174/#/top-up/simulated}") String simulationProcessUrlBase) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
        this.placeToPayClient = placeToPayClient;
        this.eventStateService = eventStateService;
        this.mapper = mapper;
        this.defaultExpirationMinutes = defaultExpirationMinutes;
        this.simulationEnabled = simulationEnabled;
        this.simulationProcessUrlBase = trimTrailingSlash(simulationProcessUrlBase);
    }

    @Transactional
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

    @Transactional
    public PaymentResponse confirm(UUID topUpId, String bearerToken) {
        WalletResponse wallet = walletClient.getCurrentWallet(bearerToken);
        Payment payment = requiredForUpdate(topUpId);
        if (!payment.getUserAccountId().equals(wallet.userId())) {
            throw new PaymentNotFoundException("Recarga no encontrada.");
        }
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return mapper.toResponse(payment);
        }
        if (payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentConflictException(
                    "La recarga no puede confirmarse en su estado actual.");
        }
        if (simulationEnabled) {
            return mapper.toResponse(credit(payment));
        }
        SessionStatus status = placeToPayClient.querySession(payment.getPlaceToPayRequestId());
        return mapper.toResponse(applyStatus(payment, status.status(), status.message()));
    }

    @Transactional
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
        if (simulationEnabled) {
            payment.assignPlaceToPaySession(
                    simulatedRequestId(payment.getId()),
                    simulationProcessUrlBase + "/" + payment.getId(),
                    reference);
            return mapper.toTopUpResponse(paymentRepository.save(payment));
        }
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

    private Payment applyStatus(Payment payment, String providerStatus, String providerMessage) {
        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.FAILED) {
            return payment;
        }
        if ("APPROVED".equalsIgnoreCase(providerStatus)) {
            return credit(payment);
        } else if ("REJECTED".equalsIgnoreCase(providerStatus)
                || "FAILED".equalsIgnoreCase(providerStatus)) {
            return eventStateService.fail(payment, providerMessage, "topup.failed");
        }
        return payment;
    }

    private Payment credit(Payment payment) {
        WalletMovementResponse movement = walletClient.credit(new WalletMovementRequest(
                payment.getUserAccountId(),
                payment.getAmount(),
                "topup:" + payment.getId() + ":credit",
                payment.getId().toString(),
                "Recarga PlaceToPay",
                "TOP_UP"));
        return eventStateService.complete(
                payment,
                movement.balanceBefore(),
                movement.balanceAfter(),
                "topup.completed");
    }

    private Payment requiredForUpdate(UUID topUpId) {
        Payment payment = paymentRepository.findByIdForUpdate(topUpId)
                .orElseThrow(() -> new PaymentNotFoundException("Recarga no encontrada."));
        if (payment.getMethod() != PaymentMethod.PLACETOPAY) {
            throw new PaymentNotFoundException("Recarga no encontrada.");
        }
        return payment;
    }

    private long simulatedRequestId(UUID paymentId) {
        long requestId = paymentId.getMostSignificantBits() & Long.MAX_VALUE;
        if (requestId == 0L) {
            requestId = paymentId.getLeastSignificantBits() & Long.MAX_VALUE;
        }
        return requestId == 0L ? 1L : requestId;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
