CREATE INDEX idx_wallet_user_id ON wallet(user_id);
CREATE INDEX idx_wallet_keycloak_id ON wallet(keycloak_id);
CREATE INDEX idx_movement_wallet_created ON wallet_movement(wallet_id, created_at DESC);
CREATE INDEX idx_movement_user_created ON wallet_movement(user_id, created_at DESC);
CREATE INDEX idx_movement_reference ON wallet_movement(reference_id);
CREATE INDEX idx_movement_created_at ON wallet_movement(created_at);
CREATE INDEX idx_movement_idempotency ON wallet_movement(idempotency_key);
