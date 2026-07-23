package com.smartpayut.payment.mapper;

import org.springframework.stereotype.Component;

import com.smartpayut.payment.domain.entity.Payment;
import com.smartpayut.payment.domain.entity.PaymentRefund;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;
import com.smartpayut.payment.dto.response.PaymentResponse;
import com.smartpayut.payment.dto.response.RefundResponse;
import com.smartpayut.payment.dto.response.TopUpResponse;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getId(),
                payment.getUserAccountId(),
                payment.getMethod(),
                payment.getStatus(),
                legacyStatus(payment),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getBusCode(),
                payment.getRouteName(),
                payment.getBalanceBefore(),
                payment.getBalanceAfter(),
                payment.getCompletedAt() == null ? payment.getCreatedAt() : payment.getCompletedAt(),
                payment.getFailureReason());
    }

    public TopUpResponse toTopUpResponse(Payment payment) {
        return new TopUpResponse(
                payment.getId(),
                "PLACETOPAY",
                payment.getStatus(),
                payment.getStatus() == PaymentStatus.COMPLETED ? "COMPLETED" : "PENDING_PAY",
                payment.getUserAccountId(),
                payment.getAmount(),
                payment.getPlaceToPayProcessUrl(),
                payment.getPlaceToPayRequestId(),
                payment.getCreatedAt());
    }

    public RefundResponse toRefundResponse(PaymentRefund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getCompletedAt());
    }

    private String legacyStatus(Payment payment) {
        return switch (payment.getStatus()) {
            case COMPLETED -> "Completado";
            case REFUNDED -> "Reembolsado";
            case FAILED -> "Fallido";
            default -> "Pendiente";
        };
    }
}
