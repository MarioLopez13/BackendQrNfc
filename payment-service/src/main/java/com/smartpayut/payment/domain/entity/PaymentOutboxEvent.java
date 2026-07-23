package com.smartpayut.payment.domain.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.smartpayut.payment.domain.enumeration.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_outbox_events")
public class PaymentOutboxEvent {

    @Id
    @Column(name = "event_id", length = 150)
    private String eventId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private OffsetDateTime nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    protected PaymentOutboxEvent() {
    }

    public PaymentOutboxEvent(String eventId, UUID paymentId, String eventType, String payload) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = now;
        this.createdAt = now;
    }

    public void markPublished() {
        status = OutboxStatus.PUBLISHED;
        publishedAt = OffsetDateTime.now(ZoneOffset.UTC);
        lastError = null;
    }

    public void scheduleRetry(String error, int delaySeconds) {
        attempts++;
        lastError = truncate(error);
        nextAttemptAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(delaySeconds);
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "Error de publicación sin detalle.";
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    public String getEventId() {
        return eventId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}
