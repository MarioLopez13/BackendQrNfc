package com.smartpayut.notification.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;

public record NotificationResponse(
        UUID id, UUID userId, NotificationType type, String title, String message,
        NotificationStatus status, NotificationSource source, String referenceId,
        BigDecimal amount, OffsetDateTime readAt, OffsetDateTime createdAt) {
}
