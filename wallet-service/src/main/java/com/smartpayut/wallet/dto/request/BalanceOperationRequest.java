package com.smartpayut.wallet.dto.request;

import com.smartpayut.wallet.domain.enumeration.MovementType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record BalanceOperationRequest(@NotNull UUID userId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 150) String idempotencyKey, @Size(max = 150) String referenceId,
        @Size(max = 500) String description, MovementType movementType) {
}
