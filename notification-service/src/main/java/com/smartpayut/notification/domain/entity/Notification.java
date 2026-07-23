package com.smartpayut.notification.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true, length = 150)
    private String eventId;

    @Column(name = "business_key", nullable = false, unique = true, length = 500)
    private String businessKey;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationSource source;

    @Column(name = "reference_id", length = 150)
    private String referenceId;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Notification() {
    }

    public Notification(
            String eventId,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            NotificationSource source,
            String referenceId,
            BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.UNREAD;
        this.source = source;
        this.referenceId = referenceId;
        this.amount = amount;
        this.businessKey = businessKey(source, type, userId, referenceId, eventId);
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markAsRead() {
        if (status == NotificationStatus.UNREAD) {
            status = NotificationStatus.READ;
            readAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public NotificationSource getSource() {
        return source;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    private String businessKey(
            NotificationSource source,
            NotificationType type,
            UUID userId,
            String referenceId,
            String eventId) {
        String businessReference = referenceId == null || referenceId.isBlank()
                ? "EVENT:" + eventId
                : referenceId;
        return source + "|" + type + "|" + userId + "|" + businessReference;
    }
}
