CREATE TABLE idempotency_keys
(
    id              BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    response_status INT          NOT NULL,
    response_body   TEXT         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_idempotency_keys_key ON idempotency_keys (idempotency_key);