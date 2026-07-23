CREATE TABLE payment_outbox_events (
    event_id VARCHAR(150) PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_payment_outbox_payment_event UNIQUE (payment_id, event_type)
);

CREATE INDEX idx_payment_outbox_pending
    ON payment_outbox_events(status, next_attempt_at, created_at);
