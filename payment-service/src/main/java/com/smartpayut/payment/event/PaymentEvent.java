package com.smartpayut.payment.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentEvent(
        String eventId,
        String eventType,
        int version,
        OffsetDateTime occurredAt,
        UUID paymentId,
        UUID userId,
        UUID walletId,
        String method,
        String status,
        BigDecimal amount,
        String currency,
        String busCode,
        String routeName,
        String failureReason) {
}
