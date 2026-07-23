package com.smartpayut.payment.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletMovementRequest(
        UUID userId,
        BigDecimal amount,
        String idempotencyKey,
        String referenceId,
        String description,
        String movementType) {
}
