package com.smartpayut.wallet.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID userId, BigDecimal balance, String currency) {
}
