CREATE TABLE user_profile (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL UNIQUE REFERENCES user_account(id),
    phone VARCHAR(40),
    avatar_url VARCHAR(500),
    preferred_language VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
