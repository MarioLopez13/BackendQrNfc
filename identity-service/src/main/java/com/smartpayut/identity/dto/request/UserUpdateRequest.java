package com.smartpayut.identity.dto.request;

import com.smartpayut.identity.domain.enumeration.UserStatus;
import jakarta.validation.constraints.Email;

public record UserUpdateRequest(String name, String lastName, @Email String email, UserStatus status, String image,
        String phone, String preferredLanguage) {
}
