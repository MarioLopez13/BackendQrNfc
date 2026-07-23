package com.smartpayut.notification.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;

public record NotificationFilter(
        UUID userId, NotificationType type, NotificationSource source,
        NotificationStatus status, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
}
