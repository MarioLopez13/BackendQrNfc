package com.smartpayut.payment.service;

import org.springframework.stereotype.Service;

import com.smartpayut.payment.client.wallet.WalletClient;
import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.entity.PaymentRefund;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.request.RefundRequest;
import com.smartpayut.payment.dto.response.RefundResponse;
import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.exception.ExternalServiceException;
import com.smartpayut.payment.exception.PaymentConflictException;
import com.smartpayut.payment.mapper.PaymentMapper;
import com.smartpayut.payment.messaging.publisher.PaymentEventPublisher;
import com.smartpayut.payment.repository.PaymentRefundRepository;
import com.smartpayut.payment.repository.PaymentRepository;

@Service
public class RefundService {

    private final PaymentQueryService queryService;
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final WalletClient walletClient;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper mapper;

    public RefundService(
            PaymentQueryService queryService,
            PaymentRepository paymentRepository,
            PaymentRefundRepository refundRepository,
            WalletClient walletClient,
            PaymentEventPublisher eventPublisher,
            PaymentMapper mapper) {
        this.queryService = queryService;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.walletClient = walletClient;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    public RefundResponse refund(java.util.UUID paymentId, RefundRequest request) {
        return refundRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(mapper::toRefundResponse)
                .orElseGet(() -> executeRefund(paymentId, request));
    }

    private RefundResponse executeRefund(java.util.UUID paymentId, RefundRequest request) {
        Payment payment = queryService.required(paymentId);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentConflictException("Solo se puede reembolsar un pago completado.");
        }

        PaymentRefund refund = refundRepository.save(
                new PaymentRefund(payment, request.reason(), request.idempotencyKey()));
        WalletMovementRequest walletRequest = new WalletMovementRequest(
                payment.getUserAccountId(),
                payment.getAmount(),
                "payment:" + payment.getId() + ":refund:" + request.idempotencyKey(),
                payment.getId().toString(),
                request.reason(),
                "PAYMENT_REFUND");
        try {
            walletClient.refund(walletRequest);
            refund.complete();
            payment.refund();
            refundRepository.save(refund);
            paymentRepository.save(payment);
            eventPublisher.publish("payment.refunded", payment);
            return mapper.toRefundResponse(refund);
        } catch (ExternalServiceException exception) {
            refund.fail(exception.getMessage());
            refundRepository.save(refund);
            throw exception;
        }
    }
}
