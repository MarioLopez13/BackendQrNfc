package com.smartpayut.identity.dto.request;

import jakarta.validation.constraints.*;

public record UserRegistrationRequest(@NotBlank @Size(max = 150) String userName,
        @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String lastName, @NotBlank @Size(min = 8, max = 100) String password) {
}
