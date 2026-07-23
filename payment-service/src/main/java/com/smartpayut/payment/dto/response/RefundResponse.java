package com.smartpayut.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.payment.domain.enumeration.RefundStatus;

public record RefundResponse(
        UUID id,
        UUID paymentId,
        BigDecimal amount,
        RefundStatus status,
        String reason,
        OffsetDateTime completedAt) {
}
