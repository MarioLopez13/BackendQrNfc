CREATE TABLE wallet_movement (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallet(id),
    user_id UUID NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    balance_before NUMERIC(19,2) NOT NULL,
    balance_after NUMERIC(19,2) NOT NULL,
    reference_id VARCHAR(150),
    idempotency_key VARCHAR(150),
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallet_movement_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_wallet_movement_amount CHECK (amount > 0),
    CONSTRAINT ck_wallet_movement_before CHECK (balance_before >= 0),
    CONSTRAINT ck_wallet_movement_after CHECK (balance_after >= 0)
);
