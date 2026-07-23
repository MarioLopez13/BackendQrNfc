CREATE TABLE transaction_records (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(150) NOT NULL UNIQUE,
    source_event_id VARCHAR(150) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id UUID,
    user_account_id UUID NOT NULL,
    wallet_id UUID,
    transaction_type VARCHAR(30) NOT NULL,
    method VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_before NUMERIC(19, 2),
    balance_after NUMERIC(19, 2),
    currency VARCHAR(3) NOT NULL,
    bus_code VARCHAR(100),
    route_name VARCHAR(250),
    failure_reason VARCHAR(500),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transaction_records_user_occurred
    ON transaction_records(user_account_id, occurred_at DESC);
CREATE INDEX idx_transaction_records_status ON transaction_records(status);
CREATE INDEX idx_transaction_records_type ON transaction_records(transaction_type);
