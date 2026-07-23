package com.smartpayut.transaction.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.transaction.domain.enumeration.TransactionSource;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;

public record TransactionResponse(
        UUID id,
        String correlationId,
        TransactionSource source,
        UUID sourceId,
        UUID userId,
        UUID walletId,
        TransactionType type,
        String method,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String currency,
        String busCode,
        String routeName,
        String failureReason,
        OffsetDateTime occurredAt) {
}
