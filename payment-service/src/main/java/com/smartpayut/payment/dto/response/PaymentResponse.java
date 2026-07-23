package com.smartpayut.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;

public record PaymentResponse(
        UUID id,
        UUID transactionId,
        UUID userId,
        PaymentMethod method,
        PaymentStatus paymentStatus,
        String status,
        BigDecimal amount,
        String currency,
        String busCode,
        String routeName,
        BigDecimal previousBalance,
        BigDecimal updatedBalance,
        OffsetDateTime processedAt,
        String failureReason) {
}
