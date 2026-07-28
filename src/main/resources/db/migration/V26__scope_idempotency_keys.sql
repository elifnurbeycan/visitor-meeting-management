ALTER TABLE idempotency_keys ADD COLUMN company_id BIGINT;
ALTER TABLE idempotency_keys ADD COLUMN user_id BIGINT;

DROP INDEX idx_idempotency_keys_key;
CREATE UNIQUE INDEX idx_idempotency_keys_scoped ON idempotency_keys(idempotency_key, company_id, user_id);