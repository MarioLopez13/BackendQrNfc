CREATE TABLE processed_events (
    event_id VARCHAR(150) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source VARCHAR(30) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_processed_events_processed_at ON processed_events(processed_at DESC);
