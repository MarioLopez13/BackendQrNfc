package com.smartpayut.payment.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.dto.request.PaymentRequest;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;
import com.smartpayut.payment.exception.ExternalServiceException;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.repository.PaymentRepository;
import com.smartpayut.payment.util.IdempotencyKeys;

@Service
public class PaymentExecutionService {

    private final PaymentRepository paymentRepository;
    private final WalletClient walletClient;
    private final PaymentEventStateService eventStateService;
    private final PaymentMapper mapper;

    public PaymentExecutionService(
            PaymentRepository paymentRepository,
            WalletClient walletClient,
            PaymentEventStateService eventStateService,
            PaymentMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
        this.eventStateService = eventStateService;
        this.mapper = mapper;
    }

    public PaymentResponse execute(
            PaymentRequest request,
            PaymentMethod method,
            String suppliedIdempotencyKey,
            String userBearerToken) {
        String idempotencyKey = IdempotencyKeys.resolve(suppliedIdempotencyKey, method.name().toLowerCase());
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return mapper.toResponse(existing.get());
        }

        WalletResponse wallet = walletClient.getCurrentWallet(userBearerToken);
        Payment payment = new Payment(
                wallet.userId(),
                wallet.id(),
                method,
                request.amount(),
                idempotencyKey,
                request.busCode(),
                request.routeName());
        payment.processing();
        paymentRepository.save(payment);

        WalletMovementRequest movementRequest = new WalletMovementRequest(
                wallet.userId(),
                request.amount(),
                "payment:" + payment.getId() + ":debit",
                payment.getId().toString(),
                "Pago " + method + " - " + request.routeName(),
                "PAYMENT_DEBIT");
        try {
            WalletMovementResponse movement = walletClient.debit(movementRequest);
            payment = eventStateService.complete(
                    payment,
                    movement.balanceBefore(),
                    movement.balanceAfter(),
                    "payment.completed");
            return mapper.toResponse(payment);
        } catch (ExternalServiceException exception) {
            eventStateService.fail(payment, exception.getMessage(), "payment.failed");
            throw exception;
        }
    }
}
