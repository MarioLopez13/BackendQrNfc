CREATE TABLE payments (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    balance_before NUMERIC(19, 2),
    balance_after NUMERIC(19, 2),
    currency VARCHAR(3) NOT NULL,
    bus_code VARCHAR(100),
    route_name VARCHAR(250),
    idempotency_key VARCHAR(150) NOT NULL UNIQUE,
    external_reference VARCHAR(150),
    placetopay_request_id BIGINT UNIQUE,
    placetopay_process_url VARCHAR(1000),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payments_user_created ON payments(user_account_id, created_at DESC);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE payment_refunds (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    idempotency_key VARCHAR(150) NOT NULL UNIQUE,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payment_refunds_payment ON payment_refunds(payment_id, created_at DESC);
