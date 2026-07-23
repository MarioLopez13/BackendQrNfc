package com.smartpayut.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundRequest(
        @NotBlank @Size(max = 150) String idempotencyKey,
        @NotBlank @Size(max = 500) String reason) {
}
