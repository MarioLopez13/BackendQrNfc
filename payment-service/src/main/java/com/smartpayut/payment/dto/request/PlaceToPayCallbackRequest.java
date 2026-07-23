package com.smartpayut.payment.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceToPayCallbackRequest(
        @NotNull Long requestId,
        @NotBlank String reference,
        @NotBlank String signature,
        @NotNull CallbackStatus status) {

    public record CallbackStatus(
            @NotBlank String status,
            @NotBlank String message,
            @NotNull OffsetDateTime date) {
    }
}
