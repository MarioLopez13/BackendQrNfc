package com.smartpayut.transaction.domain.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", length = 150)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 30)
    private String source;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, String eventType, String source) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
    }

    @PrePersist
    void onCreate() {
        processedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public String getEventId() {
        return eventId;
    }
}
