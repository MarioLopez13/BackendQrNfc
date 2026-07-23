package com.smartpayut.identity.dto.response;

import com.smartpayut.identity.domain.enumeration.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(UUID id, UUID userId, String userName, String email, String name, String lastName,
        UserStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt, String image) {
}
