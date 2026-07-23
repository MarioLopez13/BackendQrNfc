package com.smartpayut.wallet.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletEvent(UUID eventId, String eventType, int eventVersion, OffsetDateTime occurredAt, UUID walletId,
        UUID userId, UUID movementId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
        String currency, String referenceId, String idempotencyKey) {
}
