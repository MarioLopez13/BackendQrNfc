package com.smartpayut.wallet.dto.response;

import com.smartpayut.wallet.domain.enumeration.WalletStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(UUID id, UUID userId, BigDecimal balance, String currency, WalletStatus status,
        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
