CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    keycloak_id UUID NOT NULL,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_wallet_user UNIQUE (user_id),
    CONSTRAINT uk_wallet_keycloak UNIQUE (keycloak_id),
    CONSTRAINT ck_wallet_balance_nonnegative CHECK (balance >= 0),
    CONSTRAINT ck_wallet_currency CHECK (currency = 'USD'),
    CONSTRAINT ck_wallet_status CHECK (status IN ('ACTIVE','BLOCKED','CLOSED'))
);
