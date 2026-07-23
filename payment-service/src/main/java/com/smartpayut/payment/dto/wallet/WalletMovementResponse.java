package com.smartpayut.payment.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletMovementResponse(
        UUID id,
        UUID transactionId,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String referenceId) {
}
