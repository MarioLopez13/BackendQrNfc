CREATE TABLE processed_events (
    event_id VARCHAR(150) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_notification_processed_events_time ON processed_events(processed_at DESC);
