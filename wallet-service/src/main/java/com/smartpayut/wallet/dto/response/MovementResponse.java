package com.smartpayut.wallet.dto.response;

import com.smartpayut.wallet.domain.enumeration.MovementType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MovementResponse(UUID id, UUID transactionId, String title, String subtitle, String routeName,
        String busCode, BigDecimal amount, OffsetDateTime date, OffsetDateTime processedAt, String method,
        String status, MovementType type, BigDecimal balanceBefore, BigDecimal balanceAfter, String referenceId,
        String idempotencyKey) {
}
