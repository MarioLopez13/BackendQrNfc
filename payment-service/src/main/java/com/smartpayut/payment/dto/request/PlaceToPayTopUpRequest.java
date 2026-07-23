package com.smartpayut.payment.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceToPayTopUpRequest(
        @NotNull @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Min(5) @Max(1440) Integer expirationMinutes,
        @Size(max = 100) String channelCode,
        @NotBlank @Size(max = 1000) String returnUrl,
        @NotBlank @Size(max = 1000) String cancelUrl) {
}
