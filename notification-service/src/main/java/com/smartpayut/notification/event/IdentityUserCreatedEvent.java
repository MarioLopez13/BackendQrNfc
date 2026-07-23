package com.smartpayut.notification.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IdentityUserCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        OffsetDateTime occurredAt,
        UUID userId,
        UUID keycloakId,
        String userName,
        String email,
        String name,
        String lastName,
        String status) {
}
