package com.smartpayut.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(@NotBlank String username, @NotBlank String password) {
}
