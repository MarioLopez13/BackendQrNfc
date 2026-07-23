package com.smartpayut.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.payment.domain.enumeration.PaymentStatus;

public record TopUpResponse(
        UUID topUpId,
        String paymentType,
        PaymentStatus paymentStatus,
        String status,
        UUID userId,
        BigDecimal amount,
        String processUrl,
        Long requestId,
        OffsetDateTime createdAt) {
}
